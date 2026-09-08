# Capture Fabric V1

The capture fabric is the observation boundary for Crystalview.

## Runtime contract

`driver app -> accessibility event -> raw event ring -> candidate -> evidence promotion -> screenshot attempt -> canonical parse -> offer episode -> local ledger`

The parser is downstream of evidence. A parser miss must not erase the underlying observation.

## Supported platforms

Package identity is explicitly filtered through `PlatformRegistry`. The identifiers in that registry are provisional and must be verified against the installed target applications on the device before field use.

## Evidence

Each candidate receives a capture UUID and is written to local JSONL evidence. The preceding raw accessibility ring is promoted into `raw_ring_events.jsonl`. A screenshot is attempted on API 30+ when the service declares screenshot capability. Screenshot failures are recorded as statuses rather than silently discarded.

## Offer lifecycle

`OfferEpisodeManager` groups repeated accessibility updates for the same platform and normalized signature into an episode within the configured temporal window. This is deliberately separate from parser output.

## Safety boundary

This component observes only. It does not log into delivery platforms, inject gestures, auto-accept, auto-decline, spoof GPS, bypass screenshot protection, or send captured data to a remote service.

## Current limitations

The current parser is generic rather than a complete Uber/DoorDash layout adapter. The episode store is in-memory and local persistence is JSONL rather than Room. These are deliberate next-stage integration points, not hidden capabilities.
