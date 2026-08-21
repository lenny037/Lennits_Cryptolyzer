# security_spec.md
# LENNIT_CRYPTOLYZER - Security Specification

## Data Invariants
1. A user can only read and write their own profile.
2. Only users with the 'admin' or 'agent' role (as verified in their user document) can write to /executions, /memories, and /vault.
3. Agents are strictly identified; an agent can only update its own status and heartbeat.
4. All timestamps must be server-validated.
5. All IDs must match strict regex `^[a-zA-Z0-9_\-]+$`.

## The Dirty Dozen Payloads
Below are example malicious payloads that the rules MUST reject.

1. **Identity Spoofing**: non-admin trying to create an 'admin' user profile.
2. **Resource Poisoning**: injecting 1MB junk string into `agent.id`.
3. **State Shortcutting**: setting `execution.status` to 'success' without a valid risk audit.
4. **PII Leak**: standard user trying to read all user profiles.
5. **Orphaned Write**: creating a memory without a valid agent reference.
6. **Self-Assigned Admin**: user updating their own role to 'admin'.
7. **Temporal Fraud**: providing a backdated `createdAt` timestamp.
8. **Malicious ID**: using `../` in a document ID.
9. **Shadow Fields**: adding `isVerified: true` to a treasury position.
10. **Query Scraping**: unauthenticated user listing all executions.
11. **Denial of Wallet**: sending ultra-large arrays in tags.
12. **Immutable Field Write**: changing `createdAt` on an existing document.
