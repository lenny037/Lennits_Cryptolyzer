package com.lennit.cryptolyzer.contracts

/**
 * Read-only configuration port.
 *
 * Configuration is resolved through a chain (defaults, packaged file, secure storage, remote
 * override) and every lookup is typed and validated. A missing required key is a startup
 * failure, not a silent default, because silent defaults are how a testnet build ends up
 * pointed at mainnet.
 */
public interface ConfigSource {

    public fun stringOrNull(key: String): String?

    public fun requireString(key: String): Outcome<String> =
        stringOrNull(key)?.let { Outcome.success(it) }
            ?: Outcome.failure(PlatformError.Validation("Missing required config key", field = key))

    public fun requireLong(key: String): Outcome<Long> {
        val raw = stringOrNull(key)
            ?: return Outcome.failure(PlatformError.Validation("Missing required config key", field = key))
        return raw.toLongOrNull()?.let { Outcome.success(it) }
            ?: Outcome.failure(PlatformError.Validation("Config key is not a long: '$raw'", field = key))
    }

    public fun booleanOrDefault(key: String, default: Boolean): Boolean =
        when (stringOrNull(key)?.lowercase()) {
            "true", "1", "yes", "on" -> true
            "false", "0", "no", "off" -> false
            else -> default
        }

    public companion object {
        public fun of(values: Map<String, String>): ConfigSource = MapConfigSource(values)
    }
}

private class MapConfigSource(private val values: Map<String, String>) : ConfigSource {
    override fun stringOrNull(key: String): String? = values[key]
}
