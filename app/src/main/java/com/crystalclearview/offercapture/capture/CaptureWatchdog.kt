package com.crystalclearview.offercapture.capture

class CaptureWatchdog {
    data class Health(
        val lastEventMs: Long?,
        val eventsSeen: Long,
        val supportedEventsSeen: Long,
        val candidateEvents: Long
    )

    private var lastEventMs: Long? = null
    private var eventsSeen = 0L
    private var supportedEventsSeen = 0L
    private var candidateEvents = 0L

    @Synchronized
    fun recordEvent(platform: String, nowMs: Long) {
        lastEventMs = nowMs
        eventsSeen++
        if (platform.isNotBlank()) supportedEventsSeen++
    }

    @Synchronized
    fun recordCandidate() {
        candidateEvents++
    }

    @Synchronized
    fun health(): Health = Health(lastEventMs, eventsSeen, supportedEventsSeen, candidateEvents)
}
