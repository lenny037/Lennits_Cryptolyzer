package com.lennit.cryptolyzer.core.modules

class ModuleRegistry(private val modules: List<CryptolyzerModule>) {
    fun all(): List<CryptolyzerModule> = modules.toList()
    fun find(id: String): CryptolyzerModule? = modules.firstOrNull { it.id == id }

    suspend fun initializeAll(): List<ModuleHealth> = modules.map { module ->
        runCatching { module.initialize(); ModuleHealth(module.id, true) }
            .getOrElse { ModuleHealth(module.id, false, it.message ?: "initialization failed") }
    }

    suspend fun shutdownAll() = modules.asReversed().forEach { runCatching { it.shutdown() } }
}
