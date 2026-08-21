#!/usr/bin/env bash
# Fails if any Firebase, Firestore or Google Cloud dependency re-enters the buildable source tree.
#
# ADR-0001 excludes Firebase from the core. A written decision that nothing enforces is a
# suggestion, so this script runs in CI on every push. It deliberately scans only the buildable
# tree: legacy/ and docs/superseded/ are historical artefacts and are expected to mention Firebase.
set -euo pipefail

cd "$(dirname "$0")/.."

# scripts/ is excluded: the guards themselves necessarily contain the banned patterns.
readonly SCAN_DIRS=("core" "android" "buildSrc" "gradle")
readonly BANNED_PATTERNS=(
  'com\.google\.firebase'
  'com\.google\.gms'
  'com\.google\.android\.gms'
  'google-cloud-firestore'
  'firebase-admin'
  'firebase-functions'
  'firebaseio\.com'
  'firestore'
  'FirebaseApp'
  'getFirestore'
)

violations=0

for dir in "${SCAN_DIRS[@]}"; do
  [[ -d "$dir" ]] || continue
  for pattern in "${BANNED_PATTERNS[@]}"; do
    if matches=$(grep -rniE "$pattern" "$dir" \
        --include='*.kt' --include='*.kts' --include='*.java' --include='*.toml' \
        --include='*.gradle' --include='*.properties' --include='*.json' --include='*.xml' \
        --include='*.yml' 2>/dev/null); then
      echo "VIOLATION: banned pattern '$pattern' found in $dir/" >&2
      echo "$matches" >&2
      violations=$((violations + 1))
    fi
  done
done

# The google-services plugin file is how Firebase is wired into an Android build. Its presence
# anywhere in the buildable tree means the exclusion has been bypassed.
if find core android buildSrc -name 'google-services.json' -print -quit 2>/dev/null | grep -q .; then
  echo "VIOLATION: google-services.json present in the buildable tree" >&2
  violations=$((violations + 1))
fi

if [[ "$violations" -gt 0 ]]; then
  echo >&2
  echo "Firebase dependency guard failed with $violations violation group(s)." >&2
  echo "See docs/decisions/0001-no-firebase-in-core.md. If this exclusion is being" >&2
  echo "deliberately reversed, amend the ADR in the same commit." >&2
  exit 1
fi

echo "Firebase dependency guard passed: no Firebase, Firestore or GMS references in the build tree."
