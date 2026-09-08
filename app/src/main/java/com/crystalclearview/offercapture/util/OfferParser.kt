package com.crystalclearview.offercapture.util

import com.crystalclearview.offercapture.data.OfferRecord
import kotlin.math.round

object OfferParser {
    private val payoutRegex = Regex("\\$\\s*([0-9]+(?:\\.[0-9]{1,2})?)")
    private val milesRegex = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*(?:mi|mile|miles)\\b", RegexOption.IGNORE_CASE)
    private val minutesRegex = Regex("([0-9]+)\\s*(?:min|mins|minute|minutes)\\b", RegexOption.IGNORE_CASE)

    fun parse(rawText: String, sourcePackage: String, capturedAtMs: Long = System.currentTimeMillis()): OfferRecord? {
        val text = rawText.replace('\u00A0', ' ').replace(Regex("\\s+"), " ").trim()
        val payout = payoutRegex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
        val miles = milesRegex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
        val minutes = minutesRegex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
        if (payout == null || (miles == null && minutes == null)) return null

        var confidence = 0.45
        if (miles != null) confidence += 0.30
        if (minutes != null) confidence += 0.20
        if (text.contains("delivery", true) || text.contains("trip", true) || text.contains("offer", true)) {
            confidence += 0.05
        }

        val dpm = miles?.takeIf { it > 0 }?.let { round2(payout / it) }
        val dph = minutes?.takeIf { it > 0 }?.let { round2(payout / (it / 60.0)) }
        return OfferRecord(capturedAtMs, sourcePackage, text, payout, miles, minutes, dpm, dph, confidence.coerceAtMost(1.0), "parser_v1")
    }

    private fun round2(value: Double): Double = round(value * 100.0) / 100.0
}
