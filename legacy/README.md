# legacy/ — reference material, not part of the build

Nothing in this directory is compiled, tested, deployed, or depended upon. It exists so that the
rebuild can be checked against what came before, and so that the reasoning captured in
`docs/inventory/SOURCE_INVENTORY.md` and `docs/migration/LEGACY_TO_CORE_MAP.md` points at real
files rather than at the contents of a zip archive.

## Why the archives were unpacked into the tree

The default branch previously carried the project as ten committed binary archives (about 79 MB,
including a six-part split zip). That has three concrete costs: the source is invisible to diffs,
code review, and `git grep`; every clone pays the full size forever; and the same code existed in
several conflicting copies with no way to tell which was current.

The archives have been removed from the working tree and their extracted source committed as text.
The blobs still exist in git history — removing them requires a history rewrite, which is recorded
as a deliberate, separate follow-up in `docs/migration/DELETION_MANIFEST.md` rather than performed
silently here.

## Contents

| Path | What it is | Status |
| --- | --- | --- |
| `enterprise-fastapi-android/` | FastAPI backend, a Compose/Hilt/Retrofit Android client, a PWA, and infra scripts. The fullest of the three legacy trees. | Reference. Concepts migrate; code does not. |
| `react-firestore-spa/` | React 19 + Vite single-page app with `src/modules/m01..m17` TypeScript engines, persisting to Firestore. | Reference. Firestore coupling is exactly what ADR-0001 removes. |
| `_scripts/` | `bootstrap-firebase.sh` and the archive extract-and-push helpers. | Superseded. Retained as evidence of the previous direction. |
| `_stray/` | Loose `index.ts` and `LennitLogo.tsx` that sat at the repository root with no owning project. | Superseded. |

## Rules

1. Do not import from `legacy/` into any `core:*` or `android:*` module. The build cannot see it,
   and the CI guards will fail if Firebase or Firestore references reach the buildable tree.
2. When a legacy behaviour is reimplemented, record the mapping in
   `docs/migration/LEGACY_TO_CORE_MAP.md`, including what was deliberately not carried over.
3. Treat every number, threshold, and formula found here as unverified until it has a test in the
   new core. Much of the legacy Python is stub-sized (40–300 bytes per file) and several financial
   paths use binary floating point, which ADR-0003 bans.
