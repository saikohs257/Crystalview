# Blackadder / Decision Friction

Blackadder is the decision-quality arbitration layer.

## Role

It does not own capture. It does not rewrite historical records. It evaluates candidate decisions using available evidence, uncertainty, divergence, and calibration history.

## Operating mode

The initial implementation remains shadow-only:

- no autonomous accept/reject
- no live reroute
- no platform control authority
- no hidden market allow vote

## Evidence discipline

Blackadder must identify the evidence class behind each decision input and preserve the distinction between direct observation, derived state, correlation, heuristic reasoning, and speculation.

## Calibration

Decision-friction signals should be evaluated against outcome-joined history. A signal that fails to demonstrate useful predictive or decision-quality value should be demoted rather than allowed to accumulate architectural authority.

## Relationship

`Market Mirror candidate state -> Blackadder arbitration -> ShiftAssist presentation`
