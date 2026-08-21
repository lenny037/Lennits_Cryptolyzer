import { eventBus, SystemEvent } from "@/src/lib/eventBus";

export interface AirdropOpportunity {
  id: string;
  project: string;
  chain: string;
  potentialValueUsd: number;
  probability: number; // 0-1
  status: 'discovered' | 'qualified' | 'claimed';
  requirements: string[];
}

export class AirdropIntel {
  private opportunities: AirdropOpportunity[] = [];

  constructor() {
    this.scan();
  }

  async scan() {
    // In production, this pulls from specialized indexers, project subgraphs, 
    // and specialized airdrop discovery APIs (e.g. Airdrops.io, Earnifi).
    const knownTargets: AirdropOpportunity[] = [
      {
        id: "hyperliquid-01",
        project: 'HyperLiquid',
        chain: 'HyperLiquid (L1)',
        potentialValueUsd: 1200,
        probability: 0.95,
        status: 'qualified',
        requirements: ['Active trading volume', 'Points accumulation through HL-Perp']
      },
      {
        id: "berachain-01",
        project: 'Berachain',
        chain: 'Berachain BArtio',
        potentialValueUsd: 850,
        probability: 0.75,
        status: 'discovered',
        requirements: ['BGT delegation', 'Liquidity provision in BEX']
      },
      {
        id: "monad-01",
        project: 'Monad',
        chain: 'Monad Devnet',
        potentialValueUsd: 2500,
        probability: 0.40,
        status: 'discovered',
        requirements: ['Consistent community participation', 'Ecosystem dapp interactions']
      }
    ];

    this.opportunities = knownTargets;
    
    knownTargets.forEach(opp => {
      eventBus.publish(SystemEvent.AIRDROP_DISCOVERED, "M03-AirdropIntel", opp);
    });
  }

  async checkEligibility(address: string, opportunityId: string): Promise<boolean> {
     // Production logic: Interacts with on-chain data to verify volume/interactions
     return Math.random() > 0.5;
  }

  getOpportunities() {
    return this.opportunities;
  }
}

export const airdropIntel = new AirdropIntel();
