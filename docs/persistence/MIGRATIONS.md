# Schema migrations: authoring guide

This is the operational half of ADR-0010 (versioned events, forward-only migrations) and
ADR-0015 (fixture-based compatibility and migration checksums). It describes how to change the
persisted schema without breaking a database that is already installed on someone's phone.

The rule that motivates all of it: **a server's database can be dropped and rebuilt; a user's
cannot.** There is no maintenance window, no operator, and no restore path. The database on the
device is the only copy of the event log, and the event log is the audit trail.

## The two evolution axes

| Axis | Changes | Mechanism | Enforced by |
|---|---|---|---|
| Storage schema | tables, columns, indexes | `MigrationDefinition` in `Migrations.RELEASED`, applied by `SqliteMigrator` | `SqliteMigratorTest`, `SchemaFixtureCompatibilityTest` |
| Event payload | fields inside `EventEnvelope.payload` | `EventSchemaVersion` + `PayloadUpcast` in `EventRegistry.RELEASED` | `EventRegistryTest`, `RegisteredEventStoreTest`, `EventRegistryDocsTest` |

They are separate on purpose. Adding a payload field must not require a storage migration, and
the payload column stays opaque to SQL. See `docs/events/EVENT_REGISTRY.md` (generated) for the
current event contracts.

## Adding a storage migration

1. Append a new `MigrationDefinition` to `Migrations.RELEASED` with the next version number.
   **Never edit an existing one.** Its checksum is recorded in every database it built; editing it
   makes those databases unverifiable, and `SqliteMigrator` will refuse to open them.
2. Use `IF NOT EXISTS` wherever SQLite allows it, so a database left partially migrated by an older
   pre-ledger build can still complete.
3. Never delete or rewrite rows in `events`. A migration that loses rows destroys the record of what
   the system decided. Add columns, backfill them, and leave history alone.
4. Prefer additive DDL. SQLite's `ALTER TABLE` cannot drop or retype a column; the rebuild-and-copy
   dance is where migrations lose data, so avoid needing it.
5. Add a test to `SqliteMigratorTest` for anything non-obvious in the new migration.
6. Run the gate:

```bash
./gradlew :core:persistence:test --tests '*Migration*' --tests '*Fixture*'
```

### What the migrator guarantees

- Each migration runs in **its own transaction** and is recorded in the ledger
  (`schema_migrations`) in that same transaction. A crash mid-upgrade leaves the database at the
  last fully applied version, and the next open resumes.
- A **checksum mismatch** on any recorded version fails the open with `PlatformError.Storage`. This
  is deliberate: an edited shipped migration means two populations of devices have different schemas
  under the same version number, and no automatic repair is sound.
- A **database newer than the build** fails the open. A downgraded application must never migrate
  backwards or reinterpret rows written by a newer build.
- A **gap in the ledger** fails the open.
- A **pre-ledger database** (only `schema_meta`, which is what version 1 wrote) is adopted by
  backfilling the ledger with the compiled checksums, then upgraded normally.
- `schema_meta` is kept in step with the ledger so the Android/Room build can read a single integer.

## Adding a fixture for a released version

Every released schema version keeps a fixture under
`core/persistence/src/test/resources/fixtures/schema-v<N>/`. When you ship version *N*:

1. Create `schema-v<N>/events.sql` containing the DDL as of version *N*, the `schema_meta` /
   `schema_migrations` state that release wrote, and a handful of representative event rows —
   including a dead-lettered one, a retried one, and one with escaped `PayloadCodec` delimiters.
2. Fixtures are **text SQL, never `.db` files.** The repository guards reject tracked binaries and
   files over 2 MiB, and more importantly a binary fixture is unreviewable: nobody can see in a diff
   that a "fixture refresh" quietly changed the schema the test was defending.
3. Fixtures use the frozen test event contracts in `RegistryFixtures`, not production event types,
   so a fixture never depends on payload shapes that later phases are still free to ratify.
4. Add the version to `SchemaFixtureCompatibilityTest`.

**A fixture is never edited to make a test pass.** If a change breaks a fixture, the change breaks
users' databases. Fix the change.

## Adding or changing an event payload

1. Append a new `EventSchemaVersion` to the type's `EventSchema` in `EventRegistry.RELEASED` and
   give it an `upcastFromPrevious`. The declaration refuses a version above 1 without one — a stored
   row cannot upgrade itself.
2. Choose the compatibility policy honestly:
   - `AdditiveOnly` — fields are only ever added. The declaration refuses a removal.
   - `Transforming` — renames and drops are permitted, and the upcast must reconstruct the current
     shape from the old one.
3. A field's `DataClassification` may be tightened, never loosened. Redaction must not regress.
4. Producers write the **current** version. Old rows are lifted on read by `RegisteredEventStore`;
   **stored rows are never rewritten**, so an application downgrade still finds its own bytes.
5. Regenerate the registry document, which is checked by a test:

```bash
./gradlew :core:eventbus:test -Dcryptolyzer.updateEventRegistryDoc=true
```

6. Run the gate:

```bash
./gradlew :core:eventbus:test :core:persistence:test
```

### Draft versus Ratified

A `Draft` schema has an owner, a purpose and a version, but no settled payload, and accepts any
payload. Every type in `EventRegistry.RELEASED` starts as `Draft` and is ratified by the phase that
implements its producer. This is deliberate: inventing payload fields for a producer that does not
exist yet would put fiction in the contract and force the implementing phase to break its own
registry. Ratifying requires declaring the fields — the declaration refuses a `Ratified` schema with
no fields.

## What happens to an unreadable event

`RegisteredEventStore.claimPending` dead-letters an event it cannot lift, rather than dropping it or
retrying it forever. It is retained for inspection, it stops consuming retry budget, and it never
reaches a handler in a shape the handler cannot read. Diagnostic reads (`find`, `deadLetters`) return
such a row **unchanged**, because an inspection surface must show what is actually stored.

## Checklist before you open the PR

- [ ] No existing `MigrationDefinition` or `EventSchemaVersion` was edited.
- [ ] `docs/events/EVENT_REGISTRY.md` regenerated if the registry changed.
- [ ] A fixture exists for the new schema version.
- [ ] `./gradlew verifyAll` is green.
