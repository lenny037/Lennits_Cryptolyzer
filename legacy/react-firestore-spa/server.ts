import express from "express";
import path from "path";
import { createServer as createViteServer } from "vite";
import dotenv from "dotenv";

// Module Imports
import { analyticsEngine } from "./src/modules/m15-analytics/AnalyticsEngine.ts";
import { agentManager } from "./src/modules/m01-orchestration/AgentManager.ts";
import { generateSystemPlan } from "./src/modules/m17-execution/GeminiBridge.ts";
import { eventBus, SystemEvent } from "./src/lib/eventBus.ts";
import { MevEngine } from "./src/modules/m06-mev/MevEngine.ts";
import { AgentConfigSchema } from "./src/lib/schemas";

import { memoryStore } from "./src/modules/m16-memory/MemoryStore.ts";

dotenv.config();

async function startServer() {
  const app = express();
  const PORT = 3000;

  app.use(express.json());

  // API Routes
  app.get("/api/health", (req, res) => {
    res.json({ 
      status: "ok", 
      timestamp: new Date().toISOString(),
      platform: "LENNIT_CRYPTOLYZER"
    });
  });

  app.get("/api/analytics/kpis", (req, res) => {
    res.json(analyticsEngine.getKPIs());
  });

  app.get("/api/agents", (req, res) => {
    res.json(agentManager.getAgents());
  });

  app.get("/api/logs", async (req, res) => {
    try {
      const logs = await memoryStore.search("", 20); // Get latest 20 memories
      res.json(logs);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.get("/api/agents/:id/logs", async (req, res) => {
    const { id } = req.params;
    try {
      const logs = await memoryStore.getLogs(id);
      res.json(logs);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/agents/:id/start", async (req, res) => {
    const { id } = req.params;
    try {
      await agentManager.startAgent(id);
      res.json({ status: "success" });
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/agents/:id/stop", async (req, res) => {
    const { id } = req.params;
    try {
      await agentManager.stopAgent(id);
      res.json({ status: "success" });
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/agents/:id/config", async (req, res) => {
    const { id } = req.params;
    const { config } = req.body;
    try {
      const validatedConfig = AgentConfigSchema.parse(config);
      await agentManager.updateAgentConfig(id, validatedConfig);
      res.json({ status: "success" });
    } catch (error: any) {
      res.status(400).json({ error: error.errors || error.message || "Invalid configuration data" });
    }
  });

  app.get("/api/mev/discovery", async (req, res) => {
    const mev = new MevEngine();
    try {
      const opportunities = await mev.findArbitrage();
      res.json(opportunities);
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  app.post("/api/orchestrator/execute", async (req, res) => {
    const { prompt } = req.body;
    try {
      const plan = await generateSystemPlan(prompt);
      
      // Publish event
      eventBus.publish(SystemEvent.AGENT_COMMAND, "HTTP-Gateway", { 
        prompt, 
        plan,
        agentId: "m01-primary"
      });

      res.json({ status: "success", plan });
    } catch (error: any) {
      res.status(500).json({ error: error.message });
    }
  });

  // Vite middleware for development
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on http://localhost:${PORT}`);
  });
}

startServer();
