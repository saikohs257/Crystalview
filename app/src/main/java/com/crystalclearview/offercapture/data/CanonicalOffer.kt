package com.crystalclearview.offercapture.data

data class CanonicalOffer(
    val captureId: String,
    val episodeId: String,
    val platform: String,
    val capturedAtMs: Long,
    val pay: Double?,
    val miles: Double?,
    val estimatedMinutes: Double?,
    val pickup: String?,
    val destination: String?,
    val offerType: String?,
    val stacked: Boolean?,
    val shopOrPay: Boolean?,
    val parserConfidence: Double,
    val parserVersion: String,
    val rawText: String,
    val evidenceRef: String,
    val screenshotRef: String?,
    val screenshotStatus: String
)
