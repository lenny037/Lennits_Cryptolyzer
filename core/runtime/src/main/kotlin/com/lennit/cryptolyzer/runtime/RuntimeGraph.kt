package com.lennit.cryptolyzer.runtime

import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import com.lennit.cryptolyzer.contracts.RuntimeService
import com.lennit.cryptolyzer.contracts.ServiceId
import com.lennit.cryptolyzer.contracts.map

/**
 * The service dependency graph, resolved once at startup.
 *
 * This is where the Phase 1 gate "no circular dependencies" stops being a review comment and
 * becomes a failing build: [resolveStartOrder] performs a Kahn topological sort and reports the
 * exact cycle. A hand-rolled graph is used rather than a DI framework because the ordering,
 * the failure message, and the shutdown sequence are all things this platform needs to control
 * precisely, and because core must stay free of platform-specific injection machinery.
 */
public class RuntimeGraph private constructor(
    private val services: Map<ServiceId, RuntimeService>,
    public val startOrder: List<ServiceId>,
) {
    public val size: Int get() = services.size

    public fun service(id: ServiceId): RuntimeService? = services[id]

    public fun servicesInStartOrder(): List<RuntimeService> = startOrder.map { services.getValue(it) }

    public fun servicesInStopOrder(): List<RuntimeService> = servicesInStartOrder().reversed()

    public companion object {

        public fun of(services: List<RuntimeService>): Outcome<RuntimeGraph> {
            val duplicates = services.groupBy { it.id }.filterValues { it.size > 1 }.keys
            if (duplicates.isNotEmpty()) {
                return Outcome.failure(
                    PlatformError.InvariantViolation(
                        "Duplicate service ids registered: ${duplicates.joinToString()}",
                        invariant = "runtime.unique-service-id",
                    ),
                )
            }
            val byId = services.associateBy { it.id }

            val missing = services.flatMap { service ->
                service.dependsOn.filterNot(byId::containsKey).map { "${service.id} -> $it" }
            }
            if (missing.isNotEmpty()) {
                return Outcome.failure(
                    PlatformError.InvariantViolation(
                        "Unresolvable dependencies: ${missing.joinToString()}",
                        invariant = "runtime.resolvable-dependencies",
                    ),
                )
            }

            return resolveStartOrder(byId).map { order -> RuntimeGraph(byId, order) }
        }

        private fun resolveStartOrder(byId: Map<ServiceId, RuntimeService>): Outcome<List<ServiceId>> {
            val remainingDeps = byId.mapValues { (_, service) -> service.dependsOn.toMutableSet() }
                .toMutableMap()
            val order = mutableListOf<ServiceId>()

            while (remainingDeps.isNotEmpty()) {
                // Sorted for determinism: the same service set always yields the same start order,
                // which makes startup behaviour reproducible across devices and test runs.
                val ready = remainingDeps.filterValues { it.isEmpty() }.keys.sortedBy { it.value }
                if (ready.isEmpty()) {
                    return Outcome.failure(
                        PlatformError.InvariantViolation(
                            "Dependency cycle among: ${describeCycle(remainingDeps)}",
                            invariant = "runtime.acyclic-dependencies",
                        ),
                    )
                }
                ready.forEach { id ->
                    order += id
                    remainingDeps.remove(id)
                    remainingDeps.values.forEach { it.remove(id) }
                }
            }
            return Outcome.success(order)
        }

        private fun describeCycle(remaining: Map<ServiceId, Set<ServiceId>>): String =
            remaining.entries
                .sortedBy { it.key.value }
                .joinToString(", ") { (id, deps) -> "$id depends on ${deps.joinToString("+")}" }
    }
}
