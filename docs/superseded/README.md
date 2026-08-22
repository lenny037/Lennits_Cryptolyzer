# docs/superseded/ — documents that no longer govern this project

These documents are retained for provenance and are **not authoritative**. Where they conflict with
`docs/PLAN_V2.md` or anything in `docs/decisions/`, the newer document wins without exception.

Most of this material predates the current direction (Android-first, local-first, Firebase-free) and
several documents actively contradict it — `CANONICAL_MODULE_MAP.md`, `RECONSTRUCTION_PLAN.md`,
`FIREBASE_NATIVE_SCAFFOLD.md` and the `firebase-scaffold-export/` set specify Firebase Cloud
Functions, Firestore and Pub/Sub as the system core. That contradiction is the first finding in
`docs/PLAN_CRITIQUE.md`, and moving these files here is how it is resolved: a superseded document
that stays next to the current plan will eventually be followed by someone.

Nothing here should be cited as a requirement, an architecture, or a plan.
