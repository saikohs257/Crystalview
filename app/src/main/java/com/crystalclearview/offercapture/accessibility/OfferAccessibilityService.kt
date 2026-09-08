package com.crystalclearview.offercapture.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.ScreenshotResult
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.crystalclearview.offercapture.capture.CaptureTruth
import com.crystalclearview.offercapture.capture.CaptureWatchdog
import com.crystalclearview.offercapture.capture.EvidenceStore
import com.crystalclearview.offercapture.capture.OfferEpisodeManager
import com.crystalclearview.offercapture.capture.OfferSignature
import com.crystalclearview.offercapture.capture.PlatformRegistry
import com.crystalclearview.offercapture.capture.RawAccessibilityEvent
import com.crystalclearview.offercapture.capture.RawEventRingBuffer
import com.crystalclearview.offercapture.data.CaptureEvidence
import com.crystalclearview.offercapture.parser.CanonicalOfferParser
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.Executors

class OfferAccessibilityService : AccessibilityService() {
    private lateinit var store: EvidenceStore
    private val ring = RawEventRingBuffer()
    private val episodes = OfferEpisodeManager()
    private val watchdog = CaptureWatchdog()
    private val screenshotExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastCandidateMs = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = EvidenceStore(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !::store.isInitialized) return

        val packageName = event.packageName?.toString() ?: return
        val platform = PlatformRegistry.platformForPackage(packageName) ?: return
        val root = rootInActiveWindow ?: return
        val rawText = collectVisibleText(root)
        if (rawText.length < 5) return

        val now = System.currentTimeMillis()
        ring.add(RawAccessibilityEvent(now, event.eventType, packageName, rawText))
        watchdog.recordEvent(platform, now)

        val signature = OfferSignature.hash(rawText)
        val (episode, isNew) = episodes.observe(platform, signature, now)
        if (!isNew && now - lastCandidateMs < 1_000L) return

        val captureId = UUID.randomUUID().toString()
        lastCandidateMs = now
        watchdog.recordCandidate()
        val promoted = ring.promote(now)
        val frame = "$captureId:${CaptureTruth.SCHEMA_VERSION}"
        store.appendRingEvents(promoted, captureId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestScreenshot(
                captureId, episode.episodeId, platform, packageName,
                event.eventType, rawText, signature, promoted.size, frame
            )
        } else {
            persist(
                captureId, episode.episodeId, platform, packageName, event.eventType,
                rawText, signature, promoted.size, "NOT_SUPPORTED", null
            )
        }
    }

    private fun requestScreenshot(
        captureId: String,
        episodeId: String,
        platform: String,
        packageName: String,
        eventType: Int,
        rawText: String,
        signature: String,
        ringCount: Int,
        @Suppress("UNUSED_PARAMETER") frame: String
    ) {
        val file = store.screenshotFile(captureId)
        try {
            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                screenshotExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(result: ScreenshotResult) {
                        try {
                            val bitmap = Bitmap.wrapHardwareBuffer(result.hardwareBuffer, result.colorSpace)
                            if (bitmap == null) {
                                result.hardwareBuffer.close()
                                persist(captureId, episodeId, platform, packageName, eventType, rawText,
                                    signature, ringCount, "FAILED_BITMAP", null)
                                return
                            }
                            FileOutputStream(file).use { out ->
                                bitmap.copy(Bitmap.Config.ARGB_8888, false)
                                    .compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                            bitmap.recycle()
                            result.hardwareBuffer.close()
                            episodes.markScreenshot(episodeId)
                            persist(captureId, episodeId, platform, packageName, eventType, rawText,
                                signature, ringCount, "SUCCESS", file.absolutePath)
                        } catch (_: Throwable) {
                            persist(captureId, episodeId, platform, packageName, eventType, rawText,
                                signature, ringCount, "FAILED_TRANSIENT", null)
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        persist(captureId, episodeId, platform, packageName, eventType, rawText,
                            signature, ringCount, "FAILED_$errorCode", null)
                    }
                }
            )
        } catch (_: Throwable) {
            persist(captureId, episodeId, platform, packageName, eventType, rawText,
                signature, ringCount, "FAILED_EXCEPTION", null)
        }
    }

    private fun persist(
        captureId: String,
        episodeId: String,
        platform: String,
        packageName: String,
        eventType: Int,
        rawText: String,
        signature: String,
        ringCount: Int,
        screenshotStatus: String,
        screenshotRef: String?
    ) {
        mainHandler.post {
            val capturedAtMs = System.currentTimeMillis()
            val evidenceRef = "evidence.jsonl#$captureId"
            store.appendEvidence(
                CaptureEvidence(
                    captureId = captureId,
                    episodeId = episodeId,
                    platform = platform,
                    packageName = packageName,
                    capturedAtMs = capturedAtMs,
                    eventType = eventType,
                    rawText = rawText,
                    normalizedSignature = signature,
                    screenshotStatus = screenshotStatus,
                    screenshotRef = screenshotRef,
                    promotedRingEvents = ringCount
                )
            )

            CanonicalOfferParser.parse(
                rawText = rawText,
                platform = platform,
                captureId = captureId,
                episodeId = episodeId,
                capturedAtMs = capturedAtMs,
                evidenceRef = evidenceRef,
                screenshotRef = screenshotRef,
                screenshotStatus = screenshotStatus
            )?.let {
                episodes.markParserSuccess(episodeId)
                store.appendOffer(it)
            }
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        screenshotExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun collectVisibleText(root: AccessibilityNodeInfo): String {
        val out = StringBuilder()
        fun walk(node: AccessibilityNodeInfo?) {
            if (node == null) return
            if (node.isVisibleToUser) {
                node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    out.append(it).append('\n')
                }
                node.contentDescription?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    out.append(it).append('\n')
                }
            }
            for (i in 0 until node.childCount) walk(node.getChild(i))
        }
        walk(root)
        return out.toString()
    }
}
