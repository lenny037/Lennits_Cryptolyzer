#!/usr/bin/env bash
# Fails if binary floating point enters a financial code path.
#
# ADR-0003 bans Double and Float for money. Amount wraps BigDecimal precisely so that
# 0.1 + 0.2 == 0.3 holds. This guard catches the reintroduction of Double/Float in the modules
# that carry monetary values. Non-monetary Doubles (scores, ratios, latency) are allowed only in
# modules not listed here.
set -euo pipefail

cd "$(dirname "$0")/.."

readonly MONETARY_MODULES=("core/domain" "core/policy" "core/persistence")

violations=0

for module in "${MONETARY_MODULES[@]}"; do
  [[ -d "$module/src/main" ]] || continue
  # Matches declarations and casts: ': Double', ': Float', 'toDouble()', 'toFloat()', 'Math.'
  if matches=$(grep -rnE ':\s*(Double|Float)\b|\.toDouble\(\)|\.toFloat\(\)|\bMath\.' \
      "$module/src/main" --include='*.kt' 2>/dev/null | grep -v 'float-guard:allow'); then
    echo "VIOLATION: floating-point usage in monetary module $module" >&2
    echo "$matches" >&2
    violations=$((violations + 1))
  fi
done

if [[ "$violations" -gt 0 ]]; then
  echo >&2
  echo "Floating-point guard failed. See docs/decisions/0003-money-is-bigdecimal.md." >&2
  echo "Monetary values must use Amount. If a genuinely non-monetary Double is needed here," >&2
  echo "move it to a module outside the monetary set or amend the ADR." >&2
  exit 1
fi

echo "Floating-point guard passed: no Double/Float arithmetic in monetary modules."
