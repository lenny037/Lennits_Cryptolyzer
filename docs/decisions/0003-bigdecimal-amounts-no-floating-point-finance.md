# ADR-0003: Money uses BigDecimal through Amount; floating point is banned in financial paths

## Status

Accepted.

## Context

Legacy Python, TypeScript, and Android material includes float, `number`, and `Double` money values. `core:domain` provides `Amount`, wrapping `BigDecimal`, and `AssetAmount`. `Amount.toBaseUnits` fails when conversion would lose precision; treasury and cross-asset operations expose invariant failures rather than silently changing value.

## Decision

All balances, prices, costs, expected value, limits, fees, reserves, and token quantities use `Amount` and, where relevant, `AssetAmount`. Floating-point values are prohibited in financial calculation, serialization, persistence, and policy paths. Division must specify scale and rounding. A non-financial `Double`, such as a telemetry gauge or bounded algorithm parameter, is not by itself prohibited.

## Consequences

Financial behavior is exact at decimal/token precision and auditable. Callers must convert provider values at the boundary and handle parse or precision failures. BigDecimal allocation and explicit rounding require performance discipline in hot paths.

## Alternatives considered

1. IEEE-754 `Double`: rejected because it cannot reliably represent many decimal values or 18-decimal token units.
2. Integer-only base units everywhere: not selected as the universal domain representation because cross-asset display, ratios, and explicit decimal operations remain necessary.
3. String arithmetic: rejected because it moves validation and arithmetic complexity to every caller.
