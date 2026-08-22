# Code-scanning findings in `legacy/`

CodeQL runs on this repository through GitHub default setup and reports two high-severity alerts.
Both are in `legacy/`, which is reference material: it is not compiled, tested, imported, or
shipped. The alerts are recorded here rather than dismissed, because they are real defects in code
whose behaviour the rebuild is expected to reimplement, and the rebuild must not reproduce them.

| Alert | Location | What it means | Requirement on the rebuild |
| --- | --- | --- | --- |
| `js/missing-rate-limiting` | `legacy/react-firestore-spa/server.ts:130` | A route performs authorization- or resource-sensitive work with no rate limit, so it can be driven at will by an unauthenticated caller. | Any future request-handling surface must apply rate limiting at the boundary, and the limit must be covered by a test that asserts rejection, not merely configuration. |
| `js/clear-text-storage-of-sensitive-data` | `legacy/enterprise-fastapi-android/web/js/app.js:448` | Sensitive data is written to client-side storage in clear text. | Credentials, keys, and session material must go to platform-backed secure storage (PLAN_V2 Phase 8a). Web local storage is not an acceptable destination, and there is no plaintext fallback. |

## Why these are not dismissed

Dismissing an alert marks a question as answered. Deleting the legacy tree would also clear the
alerts, and would equally destroy the evidence that these mistakes were made and where. Keeping both
the code and the finding is the version that stays honest: the alerts remain visible until the
legacy tree is retired, and each one carries a stated requirement that the replacement must satisfy.

The second finding is also the strongest available argument for ADR-0008 (v1 observes and simulates,
holding no autonomous spend authority). A codebase that has already written sensitive data to clear
text should not be trusted with an unattended signing key at the same time as it is being rebuilt.

## When the legacy tree is removed

Delete this file in the same commit, and confirm the requirements above are enforced by tests in the
rebuilt modules rather than only described here.
