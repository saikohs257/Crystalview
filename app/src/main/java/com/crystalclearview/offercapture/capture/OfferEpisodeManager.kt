package com.crystalclearview.offercapture.capture

import com.crystalclearview.offercapture.data.OfferEpisode
import java.util.UUID

class OfferEpisodeManager(private val sameEpisodeWindowMs: Long = 12_000L) {
    private val active = mutableMapOf<String, OfferEpisode>()

    @Synchronized
    fun observe(platform: String, signature: String, nowMs: Long): Pair<OfferEpisode, Boolean> {
        val current = active[platform]
        if (current != null && current.signature == signature &&
            nowMs - current.lastSeenMs <= sameEpisodeWindowMs) {
            current.lastSeenMs = nowMs
            current.eventCount++
            return current to false
        }

        val episode = OfferEpisode(
            episodeId = UUID.randomUUID().toString(),
            platform = platform,
            firstSeenMs = nowMs,
            lastSeenMs = nowMs,
            eventCount = 1,
            screenshotCount = 0,
            parserSuccessCount = 0,
            signature = signature,
            state = "VISIBLE"
        )
        active[platform] = episode
        return episode to true
    }

    @Synchronized
    fun markScreenshot(episodeId: String) {
        active.values.firstOrNull { it.episodeId == episodeId }?.screenshotCount =
            (active.values.firstOrNull { it.episodeId == episodeId }?.screenshotCount ?: 0) + 1
    }

    @Synchronized
    fun markParserSuccess(episodeId: String) {
        active.values.firstOrNull { it.episodeId == episodeId }?.parserSuccessCount =
            (active.values.firstOrNull { it.episodeId == episodeId }?.parserSuccessCount ?: 0) + 1
    }
}
