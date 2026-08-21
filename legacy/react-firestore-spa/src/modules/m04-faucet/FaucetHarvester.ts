import { eventBus, SystemEvent } from "@/src/lib/eventBus";
import { withSecurityGate } from "../m12-security/RiskEngine";

export interface Faucet {
  id: string;
  name: string;
  url: string;
  frequencyHours: number;
  lastClaimed?: string;
  rewardAsset: string;
}

export class FaucetHarvester {
  private faucets: Faucet[] = [
    { id: '1', name: 'Base Sepolia Faucet', url: 'https://faucet.base.org', frequencyHours: 24, rewardAsset: 'ETH' },
    { id: '2', name: 'Sonic Faucet', url: 'https://faucet.sonic.com', frequencyHours: 12, rewardAsset: 'SONIC' }
  ];

  async claimAll() {
    const results = [];
    for (const faucet of this.faucets) {
      try {
        const res = await this.claim(faucet.id);
        results.push(res);
      } catch (e) {
        console.error(`[M04] Failed to claim ${faucet.name}`);
      }
    }
    return results;
  }

  async claim(faucetId: string) {
    const faucet = this.faucets.find(f => f.id === faucetId);
    if (!faucet) throw new Error("Faucet not found.");

    return withSecurityGate("M04-Faucet", async () => {
      console.log(`[M04][FAUCET] Claiming from ${faucet.name}...`);
      
      // Mock claim logic
      faucet.lastClaimed = new Date().toISOString();
      eventBus.publish(SystemEvent.FAUCET_CLAIMED, "M04-Faucet", { faucetId, asset: faucet.rewardAsset });
      
      return { status: "claimed", faucetId, timestamp: faucet.lastClaimed };
    }, { faucetId, type: "faucet_claim" });
  }
}

export const faucetHarvester = new FaucetHarvester();
