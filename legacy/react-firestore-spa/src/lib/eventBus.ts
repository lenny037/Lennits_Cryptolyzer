/**
 * M14 — Data Pipeline (Event Bus)
 * Authoritative Event Registry for LENNIT_CRYPTOLYZER
 */

export enum SystemEvent {
  // Agent & Orchestration
  AGENT_COMMAND = "agent.command",
  WORKFLOW_STEP = "workflow.step",
  
  // Treasury & Financial
  VAULT_UPDATE = "vault.update",
  REBALANCE_TRIGGER = "rebalance.trigger",
  STRATEGY_SIGNAL = "strategy.signal",
  
  // Intelligence
  AIRDROP_DISCOVERED = "airdrop.discovered",
  FAUCET_CLAIMED = "faucet.claimed",
  YIELD_HARVESTED = "yield.harvested",
  
  // Security & Audit
  RISK_ALERT = "risk.alert",
  TX_SUBMITTED = "tx.submitted",
  ANOMALY_DETECTED = "anomaly.detected",
  
  // Intelligence Layer
  MEMORY_STORED = "memory.stored",
  TELEMETRY_BATCH = "telemetry.batch"
}

export interface EventMetadata {
  id: string;
  timestamp: string;
  source: string;
  version: string;
}

export interface SystemPayload<T = any> {
  metadata: EventMetadata;
  payload: T;
}

export type EventCallback<T = any> = (data: SystemPayload<T>) => void;

class EventBus {
  private listeners: Map<SystemEvent, EventCallback[]> = new Map();

  subscribe<T>(event: SystemEvent, callback: EventCallback<T>) {
    const callbacks = this.listeners.get(event) || [];
    callbacks.push(callback);
    this.listeners.set(event, callbacks);
    
    return () => {
      const filtered = (this.listeners.get(event) || []).filter(cb => cb !== callback);
      this.listeners.set(event, filtered);
    };
  }

  publish<T>(event: SystemEvent, source: string, payload: T) {
    const systemPayload: SystemPayload<T> = {
      metadata: {
        id: crypto.randomUUID(),
        timestamp: new Date().toISOString(),
        source,
        version: "1.0.0"
      },
      payload
    };

    const callbacks = this.listeners.get(event) || [];
    callbacks.forEach(cb => cb(systemPayload));
    
    // In a real Firebase setup, this would also emit to Pub/Sub or Firestore Logs
    if (typeof window === "undefined") {
      console.log(`[EVENT_BUS][${event}] Produced by ${source}`);
    }
  }
}

export const eventBus = new EventBus();
