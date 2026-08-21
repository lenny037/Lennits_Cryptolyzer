#!/usr/bin/env bash
# Verifies gradle/wrapper/gradle-wrapper.jar against a known-good SHA-256.
#
# The wrapper jar is the one binary this repository tracks, and it executes on every developer
# machine and CI runner before any other project code runs. A swapped wrapper jar is therefore a
# straightforward supply-chain attack, and it would not show up in a code review of the diff.
#
# The expected value below is the published checksum for the Gradle 8.14.3 wrapper jar, listed at
# https://gradle.org/release-checksums/. gradle/actions/wrapper-validation would normally do this,
# but this repository's Actions policy permits GitHub-owned actions only, so the check is
# implemented here instead of being outsourced.
set -euo pipefail

cd "$(dirname "$0")/.."

readonly WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
readonly EXPECTED_SHA256="7d3a4ac4de1c32b59bc6a4eb8ecb8e612ccd0cf1ae1e99f66902da64df296172"

if [[ ! -f "$WRAPPER_JAR" ]]; then
  echo "VIOLATION: $WRAPPER_JAR is missing." >&2
  exit 1
fi

actual="$(sha256sum "$WRAPPER_JAR" | awk '{print $1}')"

if [[ "$actual" != "$EXPECTED_SHA256" ]]; then
  echo "VIOLATION: unexpected Gradle wrapper jar." >&2
  echo "  expected: $EXPECTED_SHA256" >&2
  echo "  actual:   $actual" >&2
  echo >&2
  echo "Do not proceed. Either the wrapper was upgraded without updating this script, or the jar" >&2
  echo "was replaced. On a deliberate Gradle upgrade, take the new checksum from" >&2
  echo "https://gradle.org/release-checksums/ rather than from the local file." >&2
  exit 1
fi

echo "Gradle wrapper verified: $actual (Gradle 8.14.3)."
