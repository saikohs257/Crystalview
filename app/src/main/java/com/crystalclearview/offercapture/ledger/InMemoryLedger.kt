package com.crystalclearview.offercapture.ledger

interface LedgerRepository {
    fun saveShift(shift: ShiftSession)
    fun saveOffer(offer: OfferObservation)
    fun saveDecision(decision: DecisionRecord)
    fun saveOutcome(outcome: TripOutcome)
    fun saveReconciliation(reconciliation: OutcomeReconciliation)
    fun getShift(shiftId: String): ShiftSession?
    fun getOffer(offerId: String): OfferObservation?
    fun getOutcome(tripId: String): TripOutcome?
    fun reconciliations(): List<OutcomeReconciliation>
}

class InMemoryLedgerRepository : LedgerRepository {
    private val shifts = linkedMapOf<String, ShiftSession>()
    private val offers = linkedMapOf<String, OfferObservation>()
    private val decisions = linkedMapOf<String, DecisionRecord>()
    private val outcomes = linkedMapOf<String, TripOutcome>()
    private val matches = linkedMapOf<String, OutcomeReconciliation>()

    override fun saveShift(shift: ShiftSession) { shifts[shift.shiftId] = shift }
    override fun saveOffer(offer: OfferObservation) { offers[offer.offerId] = offer }
    override fun saveDecision(decision: DecisionRecord) { decisions[decision.decisionId] = decision }
    override fun saveOutcome(outcome: TripOutcome) { outcomes[outcome.tripId] = outcome }
    override fun saveReconciliation(reconciliation: OutcomeReconciliation) { matches[reconciliation.reconciliationId] = reconciliation }
    override fun getShift(shiftId: String) = shifts[shiftId]
    override fun getOffer(offerId: String) = offers[offerId]
    override fun getOutcome(tripId: String) = outcomes[tripId]
    override fun reconciliations() = matches.values.toList()
}
