package com.crystalclearview.offercapture.ledger

/** Canonical longitudinal records. Raw capture evidence is never overwritten by these records. */
data class ShiftSession(
    val shiftId: String,
    val startedAtMs: Long,
    val endedAtMs: Long? = null,
    val status: ShiftStatus = ShiftStatus.ACTIVE
)

enum class ShiftStatus { ACTIVE, PAUSED, ENDED }

data class OfferObservation(
    val offerId: String,
    val captureId: String,
    val episodeId: String,
    val platform: String,
    val observedAtMs: Long,
    val pay: Double?,
    val miles: Double?,
    val estimatedMinutes: Double?,
    val pickup: String?,
    val destination: String?,
    val evidenceRefs: List<String>,
    val parserVersion: String,
    val confidence: Double
)

data class DecisionRecord(
    val decisionId: String,
    val offerId: String,
    val decidedAtMs: Long,
    val action: DecisionAction,
    val reason: String? = null
)

enum class DecisionAction { ACCEPT, DECLINE, SKIP, UNKNOWN }

data class TripOutcome(
    val tripId: String,
    val offerId: String? = null,
    val startedAtMs: Long? = null,
    val completedAtMs: Long? = null,
    val actualMiles: Double? = null,
    val durationMinutes: Double? = null,
    val grossPay: Double? = null,
    val tip: Double? = null,
    val fees: Double? = null
) {
    val netPay: Double? get() = grossPay?.let { it - (fees ?: 0.0) }
}

data class OutcomeReconciliation(
    val reconciliationId: String,
    val offerId: String,
    val tripId: String?,
    val matchStatus: MatchStatus,
    val matchMethod: MatchMethod,
    val confidence: Double,
    val offeredPay: Double?,
    val realizedPay: Double?,
    val offeredMiles: Double?,
    val realizedMiles: Double?,
    val offeredMinutes: Double?,
    val realizedMinutes: Double?,
    val payDelta: Double?,
    val milesDelta: Double?,
    val durationDeltaMinutes: Double?,
    val evidenceRefs: List<String>
)

enum class MatchStatus { MATCHED, UNRESOLVED, REJECTED }
enum class MatchMethod { EXPLICIT_OFFER_ID, PLATFORM_TRIP_LINK, TEMPORAL_ECONOMIC_MATCH, MANUAL, NONE }

data class ContextRecord(
    val contextId: String,
    val observedAtMs: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val zoneId: String? = null,
    val weatherRef: String? = null
)

data class ShiftMetrics(
    val grossEarnings: Double,
    val onlineMinutes: Double,
    val miles: Double,
    val offerCount: Int,
    val acceptedCount: Int
) {
    val grossPerOnlineHour: Double? get() = if (onlineMinutes > 0) grossEarnings / (onlineMinutes / 60.0) else null
    val grossPerMile: Double? get() = if (miles > 0) grossEarnings / miles else null
}
