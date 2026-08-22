package com.lennit.cryptolyzer.persistence

import java.security.MessageDigest

/**
 * One numbered, forward-only schema migration.
 *
 * The checksum is the point of this type. An installed application's database cannot be dropped
 * and rebuilt the way a server's can, so the statements that produced a user's schema must be
 * treated as shipped artefacts: appended to, never edited. A migration that is quietly edited after
 * release leaves two populations of devices with different schemas and the same version number,
 * and nothing in the system can tell them apart afterwards. Recording the checksum of what was
 * applied turns that class of mistake into a loud failure on the next open.
 *
 * @property version 1-based, contiguous, and permanent once released.
 * @property name short slug for the ledger and for humans reading `schema_migrations`.
 * @property statements executed in order inside a single transaction.
 */
public class MigrationDefinition(
    public val version: Int,
    public val name: String,
    public val statements: List<String>,
) {
    init {
        require(version >= 1) { "Migration version must be 1 or greater, got $version" }
        require(name.isNotBlank()) { "Migration $version must be named" }
        require(name.matches(NAME_PATTERN)) {
            "Migration name must be lowercase snake_case, got '$name'"
        }
        require(statements.isNotEmpty()) { "Migration $version ($name) has no statements" }
        require(statements.none { it.isBlank() }) { "Migration $version ($name) has a blank statement" }
    }

    /**
     * SHA-256 over the version, the name and the statements, with whitespace normalised.
     *
     * Whitespace is normalised deliberately: re-indenting a raw string is not a schema change, and
     * a checksum that fails on formatting would train people to regenerate it, which defeats the
     * control. Anything that changes the SQL itself changes the digest.
     */
    public val checksum: String by lazy {
        val canonical = buildString {
            append(version)
            append('\u001f')
            append(name)
            statements.forEach { statement ->
                append('\u001f')
                append(statement.normaliseSql())
            }
        }
        val digest = MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray())
        "sha256:" + digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    override fun toString(): String = "MigrationDefinition(v$version, $name)"

    private companion object {
        val NAME_PATTERN = Regex("^[a-z][a-z0-9_]{2,63}$")
        val WHITESPACE = Regex("\\s+")

        fun String.normaliseSql(): String = trim().replace(WHITESPACE, " ")
    }
}

/** What [SqliteMigrator.migrate] actually did, so callers and tests can assert on it. */
public class MigrationReport(
    public val fromVersion: Int,
    public val toVersion: Int,
    public val appliedVersions: List<Int>,
) {
    public val didUpgrade: Boolean get() = appliedVersions.isNotEmpty()

    override fun toString(): String =
        "MigrationReport(from=$fromVersion, to=$toVersion, applied=$appliedVersions)"
}
