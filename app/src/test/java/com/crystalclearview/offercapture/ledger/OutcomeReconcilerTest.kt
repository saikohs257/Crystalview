package com.crystalclearview.offercapture.ledger

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OutcomeReconcilerTest {
    @Test
    fun explicitOfferIdProducesMatchedReconciliation() {
        val repo = InMemoryLedgerRepository()
        repo.saveOffer(
            OfferObservation(
                offerId = "offer-1", captureId = "cap-1", episodeId = "ep-1",
                platform = "doordash", observedAtMs = 1000,
                pay = 12.0, miles = 4.0, estimatedMinutes = 20.0,
                pickup = "Store", destination = "Zone A", evidenceRefs = listOf("ev-1"),
                parserVersion = "canonical-v1", confidence = 0.95
            )
        )

        val outcome = TripOutcome(
            tripId = "trip-1", offerId = "offer-1", grossPay = 14.0,
            fees = 1.0, actualMiles = 5.0, durationMinutes = 25.0
        )
        val result = OutcomeReconciler(repo).reconcile("offer-1", outcome)

        assertEquals(MatchStatus.MATCHED, result.matchStatus)
        assertEquals(MatchMethod.EXPLICIT_OFFER_ID, result.matchMethod)
        assertEquals(1.0, result.confidence, 0.0)
        assertEquals(1.0, result.payDelta!!, 0.0)
        assertEquals(1.0, result.milesDelta!!, 0.0)
        assertEquals(5.0, result.durationDeltaMinutes!!, 0.0)
    }

    @Test
    fun missingOfferDoesNotInventAJoin() {
        val repo = InMemoryLedgerRepository()
        val result = OutcomeReconciler(repo).reconcile(
            "missing-offer",
            TripOutcome(tripId = "trip-2", offerId = "other", grossPay = 10.0)
        )

        assertEquals(MatchStatus.REJECTED, result.matchStatus)
        assertEquals(MatchMethod.NONE, result.matchMethod)
        assertNull(result.offeredPay)
        assertEquals(0.0, result.confidence, 0.0)
    }

    @Test
    fun differentTripOfferIdRemainsUnresolved() {
        val repo = InMemoryLedgerRepository()
        repo.saveOffer(
            OfferObservation(
                offerId = "offer-3", captureId = "cap-3", episodeId = "ep-3",
                platform = "uber", observedAtMs = 3000,
                pay = 9.0, miles = 3.0, estimatedMinutes = 15.0,
                pickup = null, destination = null, evidenceRefs = emptyList(),
                parserVersion = "canonical-v1", confidence = 0.8
            )
        )

        val result = OutcomeReconciler(repo).reconcile(
            "offer-3", TripOutcome(tripId = "trip-3", offerId = "another-offer", grossPay = 11.0)
        )

        assertEquals(MatchStatus.UNRESOLVED, result.matchStatus)
        assertNull(result.tripId)
        assertEquals(0.0, result.confidence, 0.0)
    }
}
