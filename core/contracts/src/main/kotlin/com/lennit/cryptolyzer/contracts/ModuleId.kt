package com.lennit.cryptolyzer.contracts

/**
 * Stable identity for every capability in the platform.
 *
 * The legacy material used bare "M06" style directory names. Opaque numbers are kept here as
 * *metadata* only: code and Gradle paths use domain names, while [code] preserves traceability
 * to the historical M00-M20 map and to the documents in docs/inventory.
 */
public enum class ModuleId(
    public val code: String,
    public val domainName: String,
    public val tier: ModuleTier,
) {
    Ingestion("M00", "ingestion", ModuleTier.Foundation),
    Orchestration("M01", "orchestration", ModuleTier.Foundation),
    Treasury("M02", "treasury", ModuleTier.Financial),
    AirdropIntelligence("M03", "airdrop-intelligence", ModuleTier.Strategy),
    RewardHarvesting("M04", "reward-harvesting", ModuleTier.Strategy),
    Prediction("M05", "prediction", ModuleTier.Intelligence),
    MarketMicrostructure("M06", "market-microstructure", ModuleTier.Strategy),
    DefiStrategy("M07", "defi-strategy", ModuleTier.Strategy),
    BlockchainDataPlane("M08", "blockchain-data-plane", ModuleTier.Foundation),
    SocialSignals("M09", "social-signals", ModuleTier.Intelligence),
    Tokenomics("M10", "tokenomics", ModuleTier.Strategy),
    Governance("M11", "governance", ModuleTier.Strategy),
    SecurityAndRisk("M12", "security-risk", ModuleTier.Foundation),
    Identity("M13", "identity", ModuleTier.Foundation),
    DataPipeline("M14", "data-pipeline", ModuleTier.Foundation),
    Analytics("M15", "analytics", ModuleTier.Intelligence),
    Memory("M16", "memory", ModuleTier.Foundation),
    AiBridge("M17", "ai-bridge", ModuleTier.Intelligence),
    MobileExperience("M18", "mobile-experience", ModuleTier.Presentation),
    Monetization("M19", "monetization", ModuleTier.Strategy),
    Learning("M20", "learning", ModuleTier.Intelligence),
    ;

    public companion object {
        public fun byCode(code: String): ModuleId =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
                ?: error("Unknown module code: $code")
    }
}

public enum class ModuleTier { Foundation, Financial, Intelligence, Strategy, Presentation }
