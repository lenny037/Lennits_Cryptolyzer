# ADR-0012: Sports-betting analytics are analysis and EV computation only

## Status

Accepted.

## Context

The owner is located in Indiana. Sports wagering, data licensing, operator access, and consumer protections are jurisdiction- and provider-dependent. `SignalKind` includes `SportsOdds`, allowing sports observations to use the same provenance and deduplication fabric as other intelligence. This record is an architectural constraint, not legal advice.

## Decision

The sports module may normalize odds signals, calculate expected value, display analytical confidence, and record outcomes. It must not place wagers, fund accounts, hold operator credentials, automate logins, or connect to a betting-account execution adapter. The feature must display an Indiana-specific jurisdiction notice and remain disabled where the required product, data, or legal review is absent.

## Consequences

Sports work reuses the Signal, Amount, prediction, and outcome contracts without becoming a separate execution system. Jurisdiction, age, geolocation, responsible-use, and data-provider questions remain release blockers for any expanded scope.

## Alternatives considered

1. Automated wagering: rejected for v1 because of custody, regulatory, platform, and responsible-use risk.
2. A separate sports data architecture: rejected because it would duplicate provenance, outcome, and analytics infrastructure.
3. No sports analytics: not selected, because read-only analysis can be bounded behind the stated controls.
