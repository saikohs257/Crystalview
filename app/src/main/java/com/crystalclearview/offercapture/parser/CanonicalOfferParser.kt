package com.crystalclearview.offercapture.parser

import com.crystalclearview.offercapture.data.CanonicalOffer
import kotlin.math.round

object CanonicalOfferParser {
    const val VERSION = "canonical_offer_parser_v1"

    private val payoutRegex = Regex("\\$\\s*([0-9]+(?:\\.[0-9]{1,2})?)")
    private val milesRegex = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*(?:mi|mile|miles)\\b", RegexOption.IGNORE_CASE)
    private val minutesRegex = Regex("([0-9]+)\\s*(?:min|mins|minute|minutes)\\b", RegexOption.IGNORE_CASE)

    fun parse(
        rawText: String,
        platform: String,
        captureId: String,
        episodeId: String,
        capturedAtMs: Long,
        evidenceRef: String,
        screenshotRef: String?,
        screenshotStatus: String
    ): CanonicalOffer? {
        val text = normalize(rawText)
        if (text.length < 5) return null

        val pay = payoutRegex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
        val miles = milesRegex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
        val minutes = minutesRegex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
        val deliveryWords = listOf("delivery", "pickup", "trip", "offer", "accept", "decline")
            .count { text.contains(it, ignoreCase = true) }

        if (pay == null || (miles == null && minutes == null && deliveryWords == 0)) return null

        var confidence = 0.20
        if (pay != null) confidence += 0.35
        if (miles != null) confidence += 0.25
        if (minutes != null) confidence += 0.15
        if (deliveryWords > 0) confidence += 0.05

        val dpm = miles?.takeIf { it > 0 }?.let { round2(pay / it) }
        val dph = minutes?.takeIf { it > 0 }?.let { round2(pay / (it / 60.0)) }

        return CanonicalOffer(
            captureId = captureId,
            episodeId = episodeId,
            platform = platform,
            capturedAtMs = capturedAtMs,
            pay = pay,
            miles = miles,
            estimatedMinutes = minutes,
            pickup = null,
            destination = null,
            offerType = null,
            stacked = null,
            shopOrPay = null,
            parserConfidence = confidence.coerceAtMost(1.0),
            parserVersion = VERSION,
            rawText = text,
            evidenceRef = evidenceRef,
            screenshotRef = screenshotRef,
            screenshotStatus = screenshotStatus
        ).also {
            @Suppress("UNUSED_VARIABLE")
            val ignoredMetrics = dpm to dph
        }
    }

    private fun normalize(rawText: String): String = rawText
        .replace('\u00A0', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun round2(value: Double): Double = round(value * 100.0) / 100.0
}
