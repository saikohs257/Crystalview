package com.crystalclearview.offercapture.capture

import java.util.UUID

data class CaptureFrame(
    val captureId: String = UUID.randomUUID().toString(),
    val platform: String,
    val packageName: String,
    val capturedAtMs: Long,
    val eventType: Int,
    val rawText: String,
    val signature: String,
    val screenshotAttempted: Boolean
)

object CaptureTruth {
    const val SCHEMA_VERSION = "capture_truth_v1"
}
