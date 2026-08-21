-- Canonical local event store schema.
-- This migration is intentionally vendor-neutral SQLite and contains no cloud dependency.

CREATE TABLE IF NOT EXISTS domain_events (
    event_id TEXT PRIMARY KEY NOT NULL,
    event_type TEXT NOT NULL,
    aggregate_id TEXT,
    occurred_at_epoch_ms INTEGER NOT NULL,
    payload_json TEXT NOT NULL,
    idempotency_key TEXT NOT NULL UNIQUE,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    available_at_epoch_ms INTEGER NOT NULL,
    processed_at_epoch_ms INTEGER,
    last_error TEXT
);

CREATE INDEX IF NOT EXISTS idx_domain_events_pending
ON domain_events (available_at_epoch_ms, occurred_at_epoch_ms)
WHERE processed_at_epoch_ms IS NULL;

CREATE INDEX IF NOT EXISTS idx_domain_events_aggregate
ON domain_events (aggregate_id, occurred_at_epoch_ms);

CREATE INDEX IF NOT EXISTS idx_domain_events_type
ON domain_events (event_type, occurred_at_epoch_ms);

-- A processed event is immutable for acknowledgement purposes. Application
-- code must acknowledge only after successful handler completion.
