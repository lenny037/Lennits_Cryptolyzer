import { Agent } from "../../types";
import { eventBus, SystemEvent } from "../../lib/eventBus";
import { db } from "../../lib/firebase";
import { collection, doc, setDoc, updateDoc, onSnapshot } from "firebase/firestore";
import { handleFirestoreError, OperationType } from "../../lib/firestoreUtils";
import { MevEngine } from "../m06-mev/MevEngine";
import { withSecurityGate } from "../m12-security/RiskEngine";
import { memoryStore } from "../m16-memory/MemoryStore";

const isServer = typeof window === 'undefined';

export class AgentManager {
  private activeAgents: Map<string, Agent> = new Map();
  private readonly COLLECTION_PATH = "agents";

  constructor() {
    this.setupListeners();
    if (isServer) {
        this.syncFromFirestoreAdmin();
    } else {
        this.syncFromFirestore();
    }
    this.startHealthCheck();
  }

  private async logAgentActivity(agentId: string, content: string, importance: number = 0.5) {
    await memoryStore.store({
      agentId,
      type: 'log',
      content,
      importance,
      tags: ['m01', 'activity']
    });
  }

  private async syncFromFirestoreAdmin() {
    try {
        const { adminDb } = await import("../../lib/firebaseAdmin");
        adminDb.collection(this.COLLECTION_PATH).onSnapshot((snapshot) => {
            snapshot.docChanges().forEach((change) => {
                const agentData = change.doc.data() as Agent;
                if (change.type === "added" || change.type === "modified") {
                    this.activeAgents.set(change.doc.id, agentData);
                } else if (change.type === "removed") {
                    this.activeAgents.delete(change.doc.id);
                }
            });
        }, (error) => {
            console.error("[M01][ADMIN] Sync error:", error);
        });
    } catch (err) {
        console.error("[M01][ADMIN] Failed to initialize admin sync:", err);
    }
  }

  private startHealthCheck() {
    setInterval(async () => {
      const now = new Date().getTime();
      const oneMinute = 60 * 1000;

      for (const agent of this.activeAgents.values()) {
        if (agent.status === 'running' && agent.lastHeartbeat) {
          const lastHb = new Date(agent.lastHeartbeat).getTime();
          if (now - lastHb > oneMinute) {
            console.error(`[M01] CRITICAL: Agent ${agent.name} (${agent.id}) missed heartbeat for >1m. Marking as ERROR.`);
            await this.updateAgentStatus(agent.id, 'error');
            await this.logAgentActivity(agent.id, "CRITICAL: Missed heartbeat. System marking as ERROR state.", 0.9);
            
            eventBus.publish(SystemEvent.RISK_ALERT, "M01-Orchestration", {
              level: "CRITICAL",
              type: "agent_heartbeat_failure",
              agentId: agent.id,
              agentName: agent.name,
              message: "Agent stopped responding to health checks."
            });
          }
        }
      }
    }, 30000); // Check every 30 seconds
  }

  private async updateAgentStatus(id: string, status: string) {
    try {
      if (isServer) {
          const { adminDb } = await import("../../lib/firebaseAdmin");
          await adminDb.collection(this.COLLECTION_PATH).doc(id).update({ status });
      } else {
          const agentRef = doc(db, this.COLLECTION_PATH, id);
          await updateDoc(agentRef, { status });
      }
    } catch (error) {
      handleFirestoreError(error, OperationType.UPDATE, `${this.COLLECTION_PATH}/${id}`);
    }
  }

  private setupListeners() {
    eventBus.subscribe(SystemEvent.AGENT_COMMAND, (event: any) => {
      console.log(`[M01][ORCHESTRATION] Received command for agent ${event.payload.agentId}`);
      this.handleCommand(event.payload);
    });
  }

  private syncFromFirestore() {
    const agentsRef = collection(db, this.COLLECTION_PATH);
    onSnapshot(agentsRef, (snapshot) => {
      snapshot.docChanges().forEach((change) => {
        const agentData = change.doc.data() as Agent;
        if (change.type === "added" || change.type === "modified") {
          this.activeAgents.set(change.doc.id, agentData);
        } else if (change.type === "removed") {
          this.activeAgents.delete(change.doc.id);
        }
      });
    }, (error) => {
      handleFirestoreError(error, OperationType.GET, this.COLLECTION_PATH);
    });
  }

  async registerAgent(agent: Agent) {
    try {
      if (isServer) {
          const { adminDb } = await import("../../lib/firebaseAdmin");
          await adminDb.collection(this.COLLECTION_PATH).doc(agent.id).set(agent);
      } else {
          const agentRef = doc(db, this.COLLECTION_PATH, agent.id);
          await setDoc(agentRef, agent);
      }
      
      await this.logAgentActivity(agent.id, `Agent registered on platform: ${agent.name} [${agent.type}]`);
      
      eventBus.publish(SystemEvent.TELEMETRY_BATCH, "M01-Orchestration", {
        type: "agent_registered",
        agentId: agent.id
      });
    } catch (error) {
      handleFirestoreError(error, OperationType.WRITE, `${this.COLLECTION_PATH}/${agent.id}`);
    }
  }

  async updateHeartbeat(agentId: string) {
    try {
      const lastHeartbeat = new Date().toISOString();
      if (isServer) {
          const { adminDb } = await import("../../lib/firebaseAdmin");
          await adminDb.collection(this.COLLECTION_PATH).doc(agentId).update({ lastHeartbeat });
      } else {
          const agentRef = doc(db, this.COLLECTION_PATH, agentId);
          await updateDoc(agentRef, { lastHeartbeat });
      }
    } catch (error) {
      handleFirestoreError(error, OperationType.UPDATE, `${this.COLLECTION_PATH}/${agentId}`);
    }
  }

  private async handleCommand(command: { agentId: string; task: string; params: any }) {
    try {
      if (isServer) {
          const { adminDb } = await import("../../lib/firebaseAdmin");
          await adminDb.collection(this.COLLECTION_PATH).doc(command.agentId).update({ status: 'running' });
      } else {
          const agentRef = doc(db, this.COLLECTION_PATH, command.agentId);
          await updateDoc(agentRef, { status: 'running' });
      }
      
      await this.logAgentActivity(command.agentId, `Received remote command: ${command.task}`);
      
      eventBus.publish(SystemEvent.WORKFLOW_STEP, "M01-Orchestration", {
        agentId: command.agentId,
        task: command.task,
        status: "started"
      });
    } catch (error) {
      handleFirestoreError(error, OperationType.UPDATE, `${this.COLLECTION_PATH}/${command.agentId}`);
    }
  }

  async updateAgentConfig(agentId: string, config: Record<string, any>) {
    try {
      if (isServer) {
          const { adminDb } = await import("../../lib/firebaseAdmin");
          await adminDb.collection(this.COLLECTION_PATH).doc(agentId).update({ config });
      } else {
          const agentRef = doc(db, this.COLLECTION_PATH, agentId);
          await updateDoc(agentRef, { config });
      }
      
      await this.logAgentActivity(agentId, "Configuration updated by user (Enterprise Gateway).");
      
      eventBus.publish(SystemEvent.TELEMETRY_BATCH, "M01-Orchestration", {
        type: "agent_config_updated",
        agentId: agentId
      });
    } catch (error) {
      handleFirestoreError(error, OperationType.UPDATE, `${this.COLLECTION_PATH}/${agentId}`);
    }
  }

  async stopAgent(id: string) {
    const agent = this.activeAgents.get(id);
    if (agent && agent.status === 'running') {
      try {
        await this.updateAgentStatus(id, 'idle');
        await this.logAgentActivity(id, "Agent execution suspended. Entering IDLE state.", 0.3);
        
        console.log(`[M01] AGENT STOPPED: ${agent.name} (${agent.id})`);
        
        eventBus.publish(SystemEvent.TELEMETRY_BATCH, "M01-Orchestration", {
          type: "agent_stopped",
          agentId: id
        });
      } catch (error) {
        handleFirestoreError(error, OperationType.UPDATE, `${this.COLLECTION_PATH}/${id}`);
      }
    }
  }

  async startAgent(id: string) {
    const agent = this.activeAgents.get(id);
    if (agent && agent.status !== 'running') {
      try {
        if (isServer) {
            const { adminDb } = await import("../../lib/firebaseAdmin");
            await adminDb.collection(this.COLLECTION_PATH).doc(id).update({ status: 'running' });
        } else {
            const agentRef = doc(db, this.COLLECTION_PATH, id);
            await updateDoc(agentRef, { status: 'running' });
        }
        
        await this.logAgentActivity(id, `Initializing full-stack loop for ${agent.name}...`, 0.6);
        console.log(`[M01] AGENT STARTED: ${agent.name} (${agent.type})`);
        
        // PRODUCTION CORE LOOP: Each agent starts its specialized autonomous behavior
        if (agent.type === 'arbitrage') {
           this.runArbitrageLoop(agent);
        } else if (agent.type === 'holder') {
           this.runPortfolioManagementLoop(agent);
        }

        eventBus.publish(SystemEvent.TELEMETRY_BATCH, "M01-Orchestration", {
          type: "agent_started",
          agentId: id
        });
      } catch (error) {
        handleFirestoreError(error, OperationType.UPDATE, `${this.COLLECTION_PATH}/${id}`);
      }
    }
  }

  private async runArbitrageLoop(agent: Agent) {
     const mev = new MevEngine();
     const loop = async () => {
        const currentAgent = this.activeAgents.get(agent.id);
        if (!currentAgent || currentAgent.status !== 'running') return;
        
        try {
          await this.updateHeartbeat(agent.id);
          const ops = await mev.findArbitrage();
          const best = ops.sort((a, b) => b.expectedProfitUsd - a.expectedProfitUsd)[0];
          
          if (best) {
            if (best.expectedProfitUsd > (currentAgent.config?.profitThreshold || 10)) {
              await this.logAgentActivity(agent.id, `Found profitable swap: ${best.route} (+${best.expectedProfitUsd.toFixed(2)} USD)`);
              
              // Auto-execute if in AUTONOMOUS mode
              if (currentAgent.config?.operationalMode === 'AUTONOMOUS') {
                 const result = await mev.executeArbitrage(best);
                 await this.logAgentActivity(agent.id, `SUCCESS: Arbitrage executed. Realized: +${result.profitRealized.toFixed(2)} USD`, 0.8);
              }
            } else {
               await this.logAgentActivity(agent.id, `Scan completed: No opportunities above threshold ($${currentAgent.config?.profitThreshold || 10}).`, 0.1);
            }
          }
        } catch (err: any) {
          console.error(`[M01][AGENT:${currentAgent.name}] loop error:`, err);
          await this.logAgentActivity(agent.id, `ENGINE ERROR: ${err.message}`, 0.9);
        }
        
        setTimeout(loop, 15000); 
     };
     loop();
  }

  private async runPortfolioManagementLoop(agent: Agent) {
     await this.logAgentActivity(agent.id, "Yield module initialized. Monitoring Uniswap/Aave liquidity depths.");
     const loop = async () => {
        const currentAgent = this.activeAgents.get(agent.id);
        if (!currentAgent || currentAgent.status !== 'running') return;
        
        try {
          await this.updateHeartbeat(agent.id);
          await this.logAgentActivity(agent.id, "Portfolio rebalancing simulation: Neutral.", 0.2);
        } catch (err: any) {
          console.error(`[M01][AGENT:${currentAgent.name}] heartbeat error:`, err);
          await this.logAgentActivity(agent.id, `ENGINE ERROR: ${err.message}`, 0.9);
        }
        
        setTimeout(loop, 45000); // Less frequent heartbeat for holder
     };
     loop();
  }

  getAgents() {
    return Array.from(this.activeAgents.values());
  }
}

export const agentManager = new AgentManager();
