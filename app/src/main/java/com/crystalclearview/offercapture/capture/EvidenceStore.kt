package com.crystalclearview.offercapture.capture

import android.content.Context
import com.crystalclearview.offercapture.data.CanonicalOffer
import com.crystalclearview.offercapture.data.CaptureEvidence
import com.crystalclearview.offercapture.capture.RawAccessibilityEvent
import org.json.JSONObject
import java.io.File

class EvidenceStore(context: Context) {
    private val root = File(context.filesDir, "capture_fabric_v1").apply { mkdirs() }
    private val evidenceFile = File(root, "evidence.jsonl")
    private val ringFile = File(root, "raw_ring_events.jsonl")
    private val offerFile = File(root, "canonical_offers.jsonl")
    private val screenshotDir = File(root, "screenshots").apply { mkdirs() }

    @Synchronized
    fun appendRingEvents(events: List<RawAccessibilityEvent>, captureId: String) {
        events.forEach { e ->
            ringFile.appendText(JSONObject().apply {
                put("capture_id", captureId)
                put("captured_at_ms", e.capturedAtMs)
                put("event_type", e.eventType)
                put("package_name", e.packageName)
                put("raw_text", e.rawText)
            }.toString() + "\n")
        }
    }

    @Synchronized
    fun appendEvidence(e: CaptureEvidence) {
        evidenceFile.appendText(JSONObject().apply {
            put("capture_id", e.captureId)
            put("episode_id", e.episodeId)
            put("platform", e.platform)
            put("package_name", e.packageName)
            put("captured_at_ms", e.capturedAtMs)
            put("event_type", e.eventType)
            put("raw_text", e.rawText)
            put("normalized_signature", e.normalizedSignature)
            put("screenshot_status", e.screenshotStatus)
            put("screenshot_ref", e.screenshotRef)
            put("promoted_ring_events", e.promotedRingEvents)
        }.toString() + "\n")
    }

    @Synchronized
    fun appendOffer(o: CanonicalOffer) {
        offerFile.appendText(JSONObject().apply {
            put("capture_id", o.captureId)
            put("episode_id", o.episodeId)
            put("platform", o.platform)
            put("captured_at_ms", o.capturedAtMs)
            put("pay", o.pay)
            put("miles", o.miles)
            put("estimated_minutes", o.estimatedMinutes)
            put("pickup", o.pickup)
            put("destination", o.destination)
            put("offer_type", o.offerType)
            put("stacked", o.stacked)
            put("shop_or_pay", o.shopOrPay)
            put("parser_confidence", o.parserConfidence)
            put("parser_version", o.parserVersion)
            put("raw_text", o.rawText)
            put("evidence_ref", o.evidenceRef)
            put("screenshot_ref", o.screenshotRef)
            put("screenshot_status", o.screenshotStatus)
        }.toString() + "\n")
    }

    fun screenshotFile(captureId: String): File = File(screenshotDir, "$captureId.png")
}
