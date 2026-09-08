# ShiftAssist

ShiftAssist is the operational presentation layer for Crystalview.

It turns already-arbitrated information into useful driver-facing assistance without becoming the capture or inference authority.

## Responsibilities

- Surface current offer facts.
- Surface relevant historical context from DriveMetrix.
- Present Market Mirror state and transition estimates with evidence class.
- Present Blackadder decision-quality/arbitration output.
- Provide arrival assistance from legitimate local context.

## Non-responsibilities

ShiftAssist does not capture raw app state, fabricate evidence, silently promote heuristics to facts, or control the delivery platform.

## Design principle

`Observe -> infer -> arbitrate -> assist`

The driver remains the decision authority.
