package com.crystalclearview.offercapture.data

data class OfferEpisode(
    val episodeId: String,
    val platform: String,
    val firstSeenMs: Long,
    var lastSeenMs: Long,
    var eventCount: Int,
    var screenshotCount: Int,
    var parserSuccessCount: Int,
    val signature: String,
    var state: String
)
