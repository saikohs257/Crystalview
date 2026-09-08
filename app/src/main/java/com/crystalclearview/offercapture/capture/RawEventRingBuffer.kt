package com.crystalclearview.offercapture.capture

import java.util.ArrayDeque

private const val DEFAULT_WINDOW_MS = 10_000L
private const val DEFAULT_MAX_EVENTS = 128

data class RawAccessibilityEvent(
    val capturedAtMs: Long,
    val eventType: Int,
    val packageName: String,
    val rawText: String
)

class RawEventRingBuffer(
    private val windowMs: Long = DEFAULT_WINDOW_MS,
    private val maxEvents: Int = DEFAULT_MAX_EVENTS
) {
    private val events = ArrayDeque<RawAccessibilityEvent>()

    @Synchronized
    fun add(event: RawAccessibilityEvent) {
        events.addLast(event)
        trim(event.capturedAtMs)
    }

    @Synchronized
    fun promote(nowMs: Long): List<RawAccessibilityEvent> {
        trim(nowMs)
        return events.toList()
    }

    @Synchronized
    fun size(): Int = events.size

    private fun trim(nowMs: Long) {
        while (events.size > maxEvents ||
            (events.isNotEmpty() && nowMs - events.first.capturedAtMs > windowMs)) {
            events.removeFirst()
        }
    }
}
