package com.crystalclearview.offercapture.data

data class OfferRecord(
    val capturedAtMs: Long,
    val sourcePackage: String,
    val rawText: String,
    val payout: Double?,
    val miles: Double?,
    val minutes: Double?,
    val dollarsPerMile: Double?,
    val dollarsPerHour: Double?,
    val parserConfidence: Double,
    val parserNotes: String
)
