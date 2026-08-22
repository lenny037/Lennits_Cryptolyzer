import { TreasuryPosition } from "@/src/types";
import { eventBus, SystemEvent } from "@/src/lib/eventBus";
import { withSecurityGate } from "../m12-security/RiskEngine";

export class TreasuryEngine {
  private positions: Map<string, TreasuryPosition> = new Map();

  constructor() {
    this.initializePositions();
  }

  private initializePositions() {
    const initial: TreasuryPosition[] = [
      { id: '1', asset: 'ETH', amount: '240.5', valueUsd: 841750, apy: 4.2, platform: 'Lido', updatedAt: new Date().toISOString() },
      { id: '2', asset: 'USDC', amount: '400756', valueUsd: 400756, apy: 12.8, platform: 'Aave V3', updatedAt: new Date().toISOString() }
    ];
    initial.forEach(p => this.positions.set(p.id, p));
  }

  async rebalance(fromId: string, toId: string, amount: string) {
    return withSecurityGate("M02-Treasury", async () => {
      console.log(`[M02][TREASURY] Rebalancing ${amount} from ${fromId} to ${toId}...`);
      
      const pFrom = this.positions.get(fromId);
      const pTo = this.positions.get(toId);

      if (!pFrom || !pTo) throw new Error("Invalid position IDs.");

      // In production, this interacts with M08 and on-chain protocols
      
      eventBus.publish(SystemEvent.VAULT_UPDATE, "M02-Treasury", { fromId, toId, amount });
      
      return { status: "success", timestamp: new Date().toISOString() };
    }, { fromId, toId, amount });
  }

  getPositions(): TreasuryPosition[] {
    return Array.from(this.positions.values());
  }
}

export const treasuryEngine = new TreasuryEngine();
