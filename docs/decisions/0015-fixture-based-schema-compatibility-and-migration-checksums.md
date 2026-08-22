# ADR-0015: Verify schema compatibility against retained text fixtures and checksum applied migrations

## Status

Accepted.

## Context

Migrations were applied by a private helper that read a single integer out of `schema_meta` and ran
whatever SQL the current build happened to compile for versions above it. Three failure modes were
invisible to that design:

1. **An edited shipped migration.** Nothing recorded what SQL had actually been applied, so a
   migration corrected after release produced two populations of devices with different schemas under
   the same version number, indistinguishable thereafter.
2. **A database from a newer build.** An older application would read a schema it did not understand
   and, worse, could apply migrations over it.
3. **A change that breaks an existing database.** Every test built its database from the current
   code, so the suite could only prove the build was self-consistent — never that it could still open
   a database written by the previous release, which is the only property users experience.

ADR-0010 required forward-only migrations. It did not say how a forward-only guarantee is verified,
and an unverified guarantee about an unmigratable device database is not worth much.

## Decision

Record and verify what was applied, and test against artefacts from the past.

- `MigrationDefinition` carries a version, a name, its statements, and a SHA-256 `checksum` over
  them with whitespace normalised. Reformatting is not a schema change; changing the SQL is.
- `SqliteMigrator` maintains a ledger table `schema_migrations(version, name, checksum, applied_at)`.
  Each migration is applied in its own transaction and recorded in that same transaction, so a crash
  mid-upgrade leaves a consistent, resumable database.
- The migrator fails the open with `PlatformError.Storage` when a recorded checksum does not match
  the compiled one, when the database version exceeds the build's target version, or when the ledger
  has gaps. None of these has a sound automatic repair, so none is attempted.
- Databases written before the ledger existed are adopted by backfilling the ledger from the compiled
  checksums; `schema_meta` is kept in step for the Android/Room build.
- `SqliteEventStore` fails construction when migration fails, and offers `openChecked` for callers
  that need the failure as an `Outcome` value rather than an exception.
- Every released schema version keeps a fixture in
  `core/persistence/src/test/resources/fixtures/schema-v<N>/events.sql`. Fixtures are **text SQL**,
  replayed into a fresh database by `SchemaFixtureCompatibilityTest`, which asserts that rows decode
  to the values they were written with, that counts are unchanged by opening, that the ledger is
  backfilled with the released checksum, that old payloads are lifted to the current event version,
  and that stored bytes are not rewritten.
- Fixtures use the frozen test contracts in `RegistryFixtures`, so a compatibility fixture never
  depends on production payload shapes that later phases are still free to ratify.
- CI runs the migration and fixture tests as a named step, so the gate is identifiable in a check
  list rather than buried inside an aggregate task.

## Consequences

A change that would break an installed database now fails a specific, named test with a message that
says what happened. The cost is real and accepted: every released schema version adds a fixture to
maintain, the checksum makes "just fix that migration" impossible by design, and a genuine
schema-repair scenario will require an explicit new migration plus a fixture rather than an edit.

`docs/persistence/MIGRATIONS.md` is the authoring guide, including the rule that a fixture is never
edited to make a test pass: if a change breaks a fixture, the change breaks users' databases.

## Alternatives considered

1. **Binary `.db` fixtures.** Rejected: the repository guards reject tracked binaries and files over
   2 MiB, and a binary fixture cannot be reviewed. A diff must be able to show that a "fixture
   refresh" changed the schema being defended.
2. **Trust the version integer alone.** Rejected: it cannot distinguish two devices whose schemas
   were built by different SQL under the same number, which is precisely the failure being addressed.
3. **Auto-repair on checksum mismatch** (re-run, or record the new checksum). Rejected: the database
   in hand may have been built either way, and guessing corrupts the one copy of the audit trail.
4. **Allow downgrade migrations.** Rejected: they double the tested surface, are almost never
   exercised, and refusing to open loses nothing while opening may destroy data.
5. **A migration framework** (Flyway, Liquibase, Room's own migration testing). Rejected for the
   core: the core stays pure Kotlin/JVM with no Android or heavyweight dependencies (ADR-0002), and
   the requirement — checksum verification plus replayable text fixtures on SQLite — is ~250 lines.
