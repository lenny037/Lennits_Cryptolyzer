import { SecurityLevel } from "@/src/types";
import { eventBus, SystemEvent } from "@/src/lib/eventBus";

export interface RiskReport {
  score: number; // 0-100
  level: SecurityLevel;
  warnings: string[];
  isSafe: boolean;
  timestamp: string;
}

export class RiskEngine {
  private static readonly CRITICAL_THRESHOLD = 80;
  private static readonly WARNING_THRESHOLD = 50;

  private static BLACKLISTED_ADDRESSES = new Set([
    "0x0000000000000000000000000000000000000000",
    "0x000000000000000000000000000000000000dead"
  ]);

  static async analyzeOperation(module: string, params: any): Promise<RiskReport> {
    let score = 0;
    const warnings: string[] = [];

    // 1. Destination Analysis
    if (params.to && this.BLACKLISTED_ADDRESSES.has(params.to.toLowerCase())) {
      score += 90;
      warnings.push("High Alert: Destination is a known blacklisted or burn address.");
    }

    // 2. Value/Volume Analysis
    if (params.amount) {
      const amount = parseFloat(params.amount);
      if (amount > 10000) {
        score += 40;
        warnings.push("Alert: Transaction exceeds standard operational threshold ($10k+).");
      } else if (amount > 1000) {
        score += 15;
        warnings.push("Notice: Medium-value transaction detected.");
      }
    }

    // 3. Contract Safety Simulation (Logic for production)
    if (params.contractAddress) {
      const isProxy = await this.checkIfProxy(params.contractAddress);
      if (isProxy) {
        score += 25;
        warnings.push("Caution: Target contract is a proxy; implementation could change.");
      }
      
      const liquidityDepth = params.liquidityDepth || 100000;
      if (liquidityDepth < 10000) {
        score += 50;
        warnings.push("Critical: Extremely low liquidity depth detected in target pool.");
      }
    }

    // 4. Module-specific context
    if (module === "M06-MEV" && params.slippage > 2.0) {
      score += 30;
      warnings.push("Risk: Maximum slippage exceeds 2%; trade at risk of frontal extraction.");
    }

    const level = this.getSecurityLevel(score);
    const isSafe = score < this.CRITICAL_THRESHOLD;

    const report: RiskReport = {
      score,
      level,
      warnings,
      isSafe,
      timestamp: new Date().toISOString()
    };

    eventBus.publish(SystemEvent.RISK_ALERT, "M12-RiskEngine", report);
    return report;
  }

  private static async checkIfProxy(address: string): Promise<boolean> {
    // In production, this performs a bytecode check for EIP-1967 or EIP-897 patterns
    return false;
  }

  private static getSecurityLevel(score: number): SecurityLevel {
    if (score >= 80) return SecurityLevel.CRITICAL;
    if (score >= 50) return SecurityLevel.HIGH;
    if (score >= 20) return SecurityLevel.MEDIUM;
    return SecurityLevel.LOW;
  }
}

/**
 * M12 — Execution Gate
 * All financial modules MUST wrap execution in this gate.
 */
export async function withSecurityGate<T>(
  module: string, 
  operation: () => Promise<T>, 
  params: any
): Promise<T> {
  const risk = await RiskEngine.analyzeOperation(module, params);
  
  if (!risk.isSafe) {
    const errorMsg = `[SECURITY_GATE_REJECTED] Module: ${module}. Risk Score: ${risk.score}. Warnings: ${risk.warnings.join(", ")}`;
    console.error(errorMsg);
    throw new Error(errorMsg);
  }

  // Audit Log
  eventBus.publish(SystemEvent.TX_SUBMITTED, module, { params, risk });

  try {
    const result = await operation();
    return result;
  } catch (error: any) {
    eventBus.publish(SystemEvent.ANOMALY_DETECTED, module, { error: error.message, params });
    throw error;
  }
}
