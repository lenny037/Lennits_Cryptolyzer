import { eventBus, SystemEvent, SystemPayload } from "@/src/lib/eventBus";

export interface SystemKPIs {
  totalValueOptimized: number;
  totalProfitRealized: number;
  activeAgentsCount: number;
  securityRiskAverage: number;
  eventFrequency: number;
}

export class AnalyticsEngine {
  private kpis: SystemKPIs = {
    totalValueOptimized: 1242506.84,
    totalProfitRealized: 45021.12,
    activeAgentsCount: 4,
    securityRiskAverage: 12,
    eventFrequency: 0
  };

  private eventCount = 0;

  constructor() {
    this.setupListeners();
  }

  private setupListeners() {
    // Listen to ALL events to calculate frequency and data shifts
    Object.values(SystemEvent).forEach(event => {
      eventBus.subscribe(event as SystemEvent, (payload) => {
        this.processEvent(event as SystemEvent, payload);
      });
    });
  }

  private processEvent(type: SystemEvent, payload: SystemPayload) {
    this.eventCount++;
    
    if (type === SystemEvent.TX_SUBMITTED) {
      this.kpis.totalProfitRealized += payload.payload.profitRealized || 0;
    }

    if (type === SystemEvent.RISK_ALERT) {
      this.kpis.securityRiskAverage = (this.kpis.securityRiskAverage + payload.payload.score) / 2;
    }
  }

  getKPIs(): SystemKPIs {
    return {
      ...this.kpis,
      eventFrequency: this.eventCount
    };
  }
}

export const analyticsEngine = new AnalyticsEngine();
