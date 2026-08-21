package com.lennit.cryptolyzer.contracts

/**
 * The platform error model.
 *
 * Every failure is classified so that callers can make a *mechanical* decision instead of
 * pattern-matching on message strings. [retryable] and [severity] drive the retry policy in
 * core:eventbus, and the fail-closed rules in core:policy treat anything unclassified as fatal.
 */
public sealed class PlatformError(
    public val code: String,
    public val message: String,
    public val retryable: Boolean,
    public val severity: Severity,
    public val cause: Throwable? = null,
) {
    public enum class Severity { Info, Warning, Error, Critical }

    /** Input failed validation. Never retryable: the same input will fail identically. */
    public class Validation(
        message: String,
        public val field: String? = null,
    ) : PlatformError("VALIDATION", message, retryable = false, severity = Severity.Warning)

    /** A dependency was reachable but rejected or misbehaved. */
    public class Upstream(
        message: String,
        public val provider: String,
        retryable: Boolean = true,
        cause: Throwable? = null,
    ) : PlatformError("UPSTREAM", message, retryable, Severity.Error, cause)

    /** Transport failure: timeout, DNS, socket, no connectivity. Always retryable. */
    public class Transport(
        message: String,
        cause: Throwable? = null,
    ) : PlatformError("TRANSPORT", message, retryable = true, severity = Severity.Warning, cause = cause)

    /** Local durable storage failed. */
    public class Storage(
        message: String,
        retryable: Boolean = false,
        cause: Throwable? = null,
    ) : PlatformError("STORAGE", message, retryable, Severity.Critical, cause)

    /** A policy or risk control refused the action. Never retryable without new inputs. */
    public class PolicyRefusal(
        message: String,
        public val ruleId: String,
    ) : PlatformError("POLICY_REFUSAL", message, retryable = false, severity = Severity.Error)

    /** An accounting or domain invariant would have been violated. Always a defect. */
    public class InvariantViolation(
        message: String,
        public val invariant: String,
    ) : PlatformError("INVARIANT", message, retryable = false, severity = Severity.Critical)

    /** Operation exceeded its deadline. */
    public class Timeout(
        message: String,
        public val elapsedMillis: Long,
    ) : PlatformError("TIMEOUT", message, retryable = true, severity = Severity.Warning)

    /**
     * Cause could not be classified. Fail-closed rules deny on this, and it must never be
     * used as a convenient catch-all in new code.
     */
    public class Unknown(
        message: String,
        cause: Throwable? = null,
    ) : PlatformError("UNKNOWN", message, retryable = false, severity = Severity.Critical, cause = cause)

    override fun toString(): String = "$code(${severity.name}, retryable=$retryable): $message"
}
