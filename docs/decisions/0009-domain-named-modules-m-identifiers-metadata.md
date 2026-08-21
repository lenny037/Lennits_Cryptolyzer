# ADR-0009: Modules are domain-named; M00–M20 identifiers are metadata

## Status

Accepted.

## Context

Legacy material uses opaque M00–M20 labels. `ModuleId` now pairs each historical code with a domain name and tier, while Gradle core modules use names such as `domain`, `eventbus`, `persistence`, and `policy`.

## Decision

Use domain-named packages, modules, directories, event prefixes, and documentation headings. Retain M00–M20 only as stable metadata for migration traceability, legacy-source mapping, and external references. New capability names must describe the domain rather than extend numeric path conventions.

## Consequences

Repository navigation and ownership reviews become clearer without losing the connection to archived source material. Some mappings remain many-to-one or deferred, so the M identifier must not be treated as a complete implementation boundary.

## Alternatives considered

1. Preserve M-numbered source directories: rejected because they conceal responsibility and encourage historic topology rather than current design.
2. Remove M identifiers entirely: rejected because the inventory and migration record need stable legacy traceability.
3. Rename only in UI documents: rejected because the source layout would remain ambiguous.
