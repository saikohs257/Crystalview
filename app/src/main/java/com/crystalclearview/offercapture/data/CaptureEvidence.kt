package com.crystalclearview.offercapture.data

data class CaptureEvidence(
    val captureId: String,
    val episodeId: String,
    val platform: String,
    val packageName: String,
    val capturedAtMs: Long,
    val eventType: Int,
    val rawText: String,
    val normalizedSignature: String,
    val screenshotStatus: String,
    val screenshotRef: String?,
    val promotedRingEvents: Int
)
