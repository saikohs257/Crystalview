package com.crystalclearview.offercapture.ledger

import java.util.UUID

/**
 * Joins an offer observation to a realized trip without mutating either source record.
 * Matching is deliberately conservative: explicit identifiers win; weak evidence stays unresolved.
 */
class OutcomeReconciler(private val repository: LedgerRepository) {
    fun reconcile(offerId: String, outcome: TripOutcome): OutcomeReconciliation {
        val offer = repository.getOffer(offerId)
            ?: return OutcomeReconciliation(
                reconciliationId = UUID.randomUUID().toString(),
                offerId = offerId,
                tripId = outcome.tripId,
                matchStatus = MatchStatus.REJECTED,
                matchMethod = MatchMethod.NONE,
                confidence = 0.0,
                offeredPay = null,
                realizedPay = outcome.netPay,
                offeredMiles = null,
                realizedMiles = outcome.actualMiles,
                offeredMinutes = null,
                realizedMinutes = outcome.durationMinutes,
                payDelta = null,
                milesDelta = null,
                durationDeltaMinutes = null,
                evidenceRefs = emptyList()
            ).also(repository::saveReconciliation)

        val explicit = outcome.offerId == offer.offerId
        val status = if (explicit) MatchStatus.MATCHED else MatchStatus.UNRESOLVED
        val method = if (explicit) MatchMethod.EXPLICIT_OFFER_ID else MatchMethod.NONE
        val confidence = if (explicit) 1.0 else 0.0
        val realizedPay = outcome.netPay ?: outcome.grossPay
        val result = OutcomeReconciliation(
            reconciliationId = UUID.randomUUID().toString(),
            offerId = offer.offerId,
            tripId = if (explicit) outcome.tripId else null,
            matchStatus = status,
            matchMethod = method,
            confidence = confidence,
            offeredPay = offer.pay,
            realizedPay = realizedPay,
            offeredMiles = offer.miles,
            realizedMiles = outcome.actualMiles,
            offeredMinutes = offer.estimatedMinutes,
            realizedMinutes = outcome.durationMinutes,
            payDelta = if (offer.pay != null && realizedPay != null) realizedPay - offer.pay else null,
            milesDelta = if (offer.miles != null && outcome.actualMiles != null) outcome.actualMiles - offer.miles else null,
            durationDeltaMinutes = if (offer.estimatedMinutes != null && outcome.durationMinutes != null) outcome.durationMinutes - offer.estimatedMinutes else null,
            evidenceRefs = offer.evidenceRefs
        )
        repository.saveReconciliation(result)
        return result
    }
}
