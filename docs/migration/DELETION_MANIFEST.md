# Deletion Manifest

This manifest describes the cleanup to perform after the extracted-source inventory and Kotlin migration are accepted. It does **not** perform any deletion, move, history rewrite, or push.

## Committed archive blobs

| Current root file | Exact size | Action | Reason |
|---|---:|---|---|
| `LENNITS_CRYPTOLYZER_ENTERPRISE.zip` | 124,218 bytes | DELETE | The enterprise tree is already extracted and inventoried; the committed binary is not a working source tree. |
| `LENNIT_CRYPTOLYZER_EXPORT.zip` | 2,251 bytes | DELETE | Its Firebase scaffold/directive material is superseded. |
| `Lennits_Cryotolyzer archives.zip.001` | 15,728,640 bytes | DELETE | Split legacy archive; source has been extracted and inventoried. |
| `Lennits_Cryotolyzer archives.zip.002` | 15,728,640 bytes | DELETE | Split legacy archive; source has been extracted and inventoried. |
| `Lennits_Cryotolyzer archives.zip.003` | 15,728,640 bytes | DELETE | Split legacy archive; source has been extracted and inventoried. |
| `Lennits_Cryotolyzer archives.zip.004` | 15,728,640 bytes | DELETE | Split legacy archive; source has been extracted and inventoried. |
| `Lennits_Cryotolyzer archives.zip.005` | 15,728,640 bytes | DELETE | Split legacy archive; source has been extracted and inventoried. |
| `Lennits_Cryotolyzer archives.zip.006` | 1,227,047 bytes | DELETE | Final split segment; source has been extracted and inventoried. |
| `Lennits_Cryptolyzer1-main.zip` | 104,543 bytes | DELETE | React/Firebase source is already extracted and is not the selected runtime. |

Deleting these files from `main` does **not** remove their blobs from existing Git history. If repository size reduction is required, schedule a separate, reviewed history-rewrite follow-up (for example, a filter-repo/BFG plan, force-push coordination, clone invalidation, and credential review). Do not perform that rewrite as part of this cleanup.

## Firebase scaffold and bootstrap material

| Current root file | Action | Destination / reason |
|---|---|---|
| `bootstrap-firebase.sh` | DELETE | Initializes Firebase Functions, Firestore, Hosting, and Firebase-specific directories; core no longer uses Firebase. |
| `FIREBASE_NATIVE_SCAFFOLD.md` | MOVE-TO-`docs/superseded/` | Historical Firebase runtime model only. |
| `CANONICAL_REPOSITORY_RECONSTRUCTION_DIRECTIVE.md` | MOVE-TO-`docs/superseded/` | Reconstruction directive predates the Kotlin target and contains no current implementation authority. |
| `EXTRACTION_QUICKSTART.md` | MOVE-TO-`docs/superseded/` | Archive extraction/push procedure is historical and instructs Git operations. |

## Superseded planning material

| Current root file | Action | Destination / reason |
|---|---|---|
| `00_MASTER_SYSTEM_CONTEXT.md` | MOVE-TO-`docs/superseded/` | Describes the platform as Firebase-native. |
| `CANONICAL_MODULE_MAP.md` | MOVE-TO-`docs/superseded/` | Maps M01–M20 to Cloud Functions, Firestore, Pub/Sub, and schedulers. Retain M-identities only through the new migration map. |
| `MASTER_INVENTORY.md` | MOVE-TO-`docs/superseded/` | Earlier archive inventory is replaced by `docs/inventory/SOURCE_INVENTORY.md`. |
| `RECONSTRUCTION_PLAN.md` | MOVE-TO-`docs/superseded/` | Plans Python-to-TypeScript and Firebase-native modernization, which is superseded. |
| `SYSTEM_RELATIONSHIP_MAP.md` | MOVE-TO-`docs/superseded/` | Uses Cloud Functions/FastAPI, Pub/Sub, and Firestore topology. |
| `DUPLICATE_CLUSTER_REPORT.md` | MOVE-TO-`docs/superseded/` | Replaced by evidence-based `docs/inventory/DUPLICATE_CLUSTERS.md` for the Kotlin direction. |
| `LENNIT_CRYPTOLYZER_KIMI_KNOWLEDGE_PACK_v1.md` | MOVE-TO-`docs/superseded/` | Historical AI-ingestion/Kimi context, not current architecture authority. |
| `201-kimi-platform-architect.md` | MOVE-TO-`docs/superseded/` | Historical specialist prompt. |
| `202-kimi-agent-orchestratormd.md` | MOVE-TO-`docs/superseded/` | Historical specialist prompt. |
| `203-kimi-backend-services.md` | MOVE-TO-`docs/superseded/` | Historical specialist prompt. |
| `204-kimi-blockchain-and-defi.md` | MOVE-TO-`docs/superseded/` | Historical specialist prompt. |
| `205-kimi-prediction-and-intelligence.md` | MOVE-TO-`docs/superseded/` | Historical specialist prompt. |
| `206-kimi-security-risk-governance.md` | MOVE-TO-`docs/superseded/` | Historical specialist prompt. |
| `207-kimi-frontend-mobile-opsmd.md` | MOVE-TO-`docs/superseded/` | Historical specialist prompt. |
| `208-kimi-devops-observability.md` | MOVE-TO-`docs/superseded/` | Historical specialist prompt. |
| `209-kimi-docs-contracts-tests.md` | MOVE-TO-`docs/superseded/` | Historical specialist prompt. |
| `210-kimi-upload-order-and-usage.md` | MOVE-TO-`docs/superseded/` | Historical upload-order instructions. |

## Stray root files

| Current root file | Action | Destination / reason |
|---|---|---|
| `index.ts` | MOVE-TO-`legacy/` | Firebase Admin/Google Cloud structured logger is source material, but it is orphaned at root and incompatible with the local-first core. |
| `LennitLogo.tsx` | MOVE-TO-`legacy/` | React SVG asset implementation is a visual reference, not part of the Android-first build. |
| `Lock.com_ Crypto Wallet & Quantum Secure` | DELETE | Saved third-party MHTML browser snapshot; unrelated to the product source tree. |
| `extract-and-push.sh` | DELETE | Historical extraction script that organizes an extracted tree and invokes Git workflows. |
| `extract-and-push.ps1` | DELETE | Historical Windows extraction/push script; its shebang/content mix is not a current build artifact. |

The root `settings.gradle.kts` is deliberately absent: it already declares the intended Kotlin `core:*` modules and `:android:app`, so it is not a stray file under this manifest.
