/**
 * MODULE 18: LENNITS CRYPTOLYZER — Main PWA App Controller
 * Handles routing, API communication, and UI rendering.
 */

"use strict";

// ── Config ─────────────────────────────────────────────────────────────────
let API_BASE = (() => {
  // Check Android bridge first, then localStorage, then empty
  if (typeof LennitBridge !== "undefined") {
    return LennitBridge.getSetting("api_endpoint") || "";
  }
  return localStorage.getItem("api_endpoint") || "";
})();

let WS_URL    = API_BASE.replace(/^http/, "ws") + "/ws/metrics";
let ws        = null;
let wsRetries = 0;

// ── Service Worker ─────────────────────────────────────────────────────────
if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("/sw.js").catch(console.error);
  });
}

// ── Splash → Main transition ────────────────────────────────────────────────
window.addEventListener("load", () => {
  setTimeout(() => {
    const splash = document.getElementById("splash");
    const main   = document.getElementById("main");
    splash.style.opacity = "0";
    setTimeout(() => {
      splash.style.display = "none";
      main.classList.remove("hidden");
      navigate("dashboard");
      connectWebSocket();
    }, 400);
  }, 1800);
});

// ── Navigation ─────────────────────────────────────────────────────────────
const screens = document.querySelectorAll(".screen");
const navBtns = document.querySelectorAll(".nav-btn");

function navigate(name) {
  screens.forEach(s => s.classList.toggle("active", s.id === `screen-${name}`));
  navBtns.forEach(b => b.classList.toggle("active", b.dataset.screen === name));
  loadScreen(name);
}

navBtns.forEach(btn => btn.addEventListener("click", () => navigate(btn.dataset.screen)));

// ── Screen Loaders ─────────────────────────────────────────────────────────
async function loadScreen(name) {
  try {
    switch (name) {
      case "dashboard":     return await loadDashboard();
      case "agents":        return await loadAgents();
      case "vault":         return await loadVault();
      case "strategies":    return await loadStrategies();
      case "notifications": return await loadNotifications();
      case "safe":          return renderSafe();
      case "settings":      return renderSettings();
    }
  } catch (err) {
    renderError(`screen-${name}`, err.message);
  }
}

// ── API Helper ─────────────────────────────────────────────────────────────
async function apiFetch(path, opts = {}) {
  if (!API_BASE) {
    // Return mock data when no API configured
    return getMockData(path);
  }
  const apiKey = localStorage.getItem("api_key") || "";
  const res    = await fetch(`${API_BASE}${path}`, {
    headers: { "X-API-Key": apiKey, "Content-Type": "application/json" },
    ...opts,
  });
  if (!res.ok) throw new Error(`API ${res.status}: ${path}`);
  return res.json();
}

// ── MODULE 02: Dashboard ───────────────────────────────────────────────────
async function loadDashboard() {
  const el = document.getElementById("screen-dashboard");
  const [dash, metrics] = await Promise.all([
    apiFetch("/api/v1/dashboard"),
    apiFetch("/api/v1/portfolio/metrics"),
  ]);

  el.innerHTML = `
    <div class="fade-in">
      <div class="card">
        <div class="card-title">Total Portfolio</div>
        <div class="card-value" id="totalPortfolio">${dash.total_portfolio_usd ?? "—"}</div>
        <div class="card-sub ${(dash.pnl_24h_usd ?? "").startsWith("-") ? "loss" : "profit"}">
          ${dash.pnl_24h_usd ?? "—"} (${dash.pnl_24h_pct ?? "—"})
        </div>
      </div>

      <div class="card-row">
        <div class="card">
          <div class="card-title">Agents</div>
          <div class="card-value" style="color:var(--green)">${dash.active_agents ?? "—"}/${dash.total_agents ?? 20}</div>
          <div class="card-sub">AlphaGrid</div>
        </div>
        <div class="card">
          <div class="card-title">Status</div>
          <div class="card-value" style="font-size:16px;color:var(--green)">${dash.system_status ?? "—"}</div>
          <div class="card-sub">Sovereign Loop</div>
        </div>
      </div>

      <div class="card-row">
        <div class="card">
          <div class="card-title">Sharpe Ratio</div>
          <div class="card-value" style="color:var(--copper)">${(metrics.sharpe_ratio ?? 0).toFixed(2)}</div>
        </div>
        <div class="card">
          <div class="card-title">Max Drawdown</div>
          <div class="card-value ${metrics.drawdown_pct < 0 ? 'loss' : 'profit'}">${(metrics.drawdown_pct ?? 0).toFixed(1)}%</div>
        </div>
      </div>

      <div class="card">
        <div class="card-title">Performance</div>
        <div class="chart-container"><canvas id="perfChart" class="chart"></canvas></div>
      </div>

      <div class="card-row">
        <div class="card">
          <div class="card-title">Weekly PnL</div>
          <div class="card-value profit">+$${(metrics.weekly_pnl ?? 0).toLocaleString()}</div>
        </div>
        <div class="card">
          <div class="card-title">Monthly PnL</div>
          <div class="card-value profit">+$${(metrics.monthly_pnl ?? 0).toLocaleString()}</div>
        </div>
      </div>
    </div>
  `;

  // Draw chart after DOM insert
  if (window.CL_Charts) window.CL_Charts.drawPerformanceChart("perfChart");
}

// ── MODULE 03: Agents ──────────────────────────────────────────────────────
async function loadAgents() {
  const el   = document.getElementById("screen-agents");
  const data = await apiFetch("/api/v1/agents");
  const agents = data.agents ?? data;

  const running = agents.filter(a => a.status === "RUNNING").length;

  el.innerHTML = `
    <div class="fade-in">
      <div class="card">
        <div class="card-title">AlphaGrid Status</div>
        <div class="card-value" style="color:var(--green)">${running}/20 Active</div>
        <div class="card-sub">3 roles: ARB • HiMAP • DarkForest</div>
      </div>
      <p class="section-title">Agent Roster</p>
      <ul id="agentList" style="list-style:none;padding:0">
        ${agents.map(a => agentCard(a)).join("")}
      </ul>
    </div>
  `;
}

function agentCard(a) {
  const roleMap = { ARBITRAGE:"arb", HIMAP:"himap", DARKFOREST:"guard" };
  const cls     = roleMap[a.role] ?? "arb";
  const roleLabel = a.role === "DARKFOREST" ? "GUARD" : a.role;
  return `
    <li class="item-card">
      <div>
        <div class="item-name">${esc(a.id)}</div>
        <div class="item-meta">
          <span class="badge badge-${cls}">${roleLabel}</span>
          &nbsp; +$${(a.profit_usd ?? 0).toFixed(2)}
        </div>
        <div class="item-meta" style="font-size:11px">${esc(a.last_action ?? "idle")}</div>
      </div>
      <div style="display:flex;flex-direction:column;align-items:flex-end;gap:6px">
        <span class="badge badge-${a.status.toLowerCase()}">${a.status}</span>
        <button class="btn btn-outline btn-sm"
          onclick="controlAgent('${a.id}','${a.status==='RUNNING'?'PAUSE':'START'}')">
          ${a.status === "RUNNING" ? "⏸ Pause" : "▶ Start"}
        </button>
      </div>
    </li>`;
}

async function controlAgent(id, action) {
  await apiFetch(`/api/v1/agents/${id}/control`, {
    method: "POST",
    body: JSON.stringify({ action }),
  });
  await loadAgents();
}

// ── MODULE 04: Vault ───────────────────────────────────────────────────────
async function loadVault() {
  const el   = document.getElementById("screen-vault");
  const data = await apiFetch("/api/v1/vault");
  const positions = data.positions ?? data;
  const total = positions.reduce((s, p) => s + (p.usd_value ?? 0), 0);

  el.innerHTML = `
    <div class="fade-in">
      <div class="card">
        <div class="card-title">Total Vault Value</div>
        <div class="card-value">$${total.toLocaleString("en-US", {minimumFractionDigits:2})}</div>
        <div class="card-sub">${positions.length} assets · ${[...new Set(positions.map(p=>p.chain))].length} chains</div>
      </div>
      <p class="section-title">Vault Positions</p>
      <ul id="vaultList" style="list-style:none;padding:0">
        ${positions.map(p => vaultCard(p)).join("")}
      </ul>
      <div style="display:flex;gap:10px">
        <button class="btn btn-copper" style="flex:1" onclick="triggerRebalance()">⚖ Rebalance</button>
        <button class="btn btn-outline" style="flex:1" onclick="triggerWithdraw()">💸 Withdraw</button>
      </div>
    </div>
  `;
}

function vaultCard(p) {
  return `
    <li class="item-card">
      <div>
        <div class="item-name" style="color:var(--copper-light)">${esc(p.symbol)} — ${esc(p.name)}</div>
        <div class="item-meta">${p.amount} · ${p.chain}</div>
        ${p.apy ? `<div class="item-meta profit">APY: ${p.apy}%</div>` : ""}
      </div>
      <div class="item-value">$${(p.usd_value ?? 0).toLocaleString("en-US",{minimumFractionDigits:2})}</div>
    </li>`;
}

async function triggerRebalance() {
  await apiFetch("/api/v1/vault/rebalance", { method: "POST" });
  showToast("Rebalance queued");
}
async function triggerWithdraw() {
  await apiFetch("/api/v1/vault/withdraw", { method: "POST" });
  showToast("Withdrawal queued");
}

// ── MODULE 05: Strategies ──────────────────────────────────────────────────
async function loadStrategies() {
  const el   = document.getElementById("screen-strategies");
  const data = await apiFetch("/api/v1/strategies");
  const strategies = data.strategies ?? data;

  el.innerHTML = `
    <div class="fade-in">
      <p class="section-title">Active Strategies</p>
      <ul id="stratList" style="list-style:none;padding:0">
        ${strategies.map(s => strategyCard(s)).join("")}
      </ul>
    </div>
  `;
}

function strategyCard(s) {
  const typeColors = {ARBITRAGE:"gold",YIELD:"green",AIRDROP:"copper",MEV:"red",CROSS_CHAIN:"orange"};
  const c = typeColors[s.type] ?? "grey";
  return `
    <li class="item-card" style="flex-direction:column;align-items:stretch;gap:10px">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>
          <div class="item-name">${esc(s.name)}</div>
          <span class="badge" style="background:rgba(var(--${c}-rgb,255,215,0),0.15);color:var(--${c},var(--gold))">${s.type}</span>
        </div>
        <span class="badge badge-${s.mode.toLowerCase()}">${s.mode}</span>
      </div>
      <div style="display:flex;justify-content:space-between">
        <div><div class="item-meta">Allocated</div><div style="color:var(--white);font-weight:600">$${(s.allocated_usd??0).toLocaleString()}</div></div>
        <div><div class="item-meta">24h PnL</div><div class="profit">+$${(s.pnl_24h??0).toFixed(2)}</div></div>
        <div><div class="item-meta">Win Rate</div><div style="color:var(--copper-light)">${((s.win_rate??0)*100).toFixed(0)}%</div></div>
      </div>
      <div style="display:flex;gap:6px">
        ${["LIVE","SHADOW","OFF"].map(m => `
          <button class="btn btn-sm ${s.mode===m?'btn-copper':'btn-outline'}"
            onclick="setStrategyMode('${s.id}','${m}')">${m}</button>`).join("")}
      </div>
    </li>`;
}

async function setStrategyMode(id, mode) {
  await apiFetch("/api/v1/strategies/mode", {
    method:"POST",
    body: JSON.stringify({strategy_id:id, mode}),
  });
  await loadStrategies();
}

// ── MODULE 06: Notifications ───────────────────────────────────────────────
async function loadNotifications() {
  const el   = document.getElementById("screen-notifications");
  const data = await apiFetch("/api/v1/notifications?limit=50");
  const items = data.notifications ?? (Array.isArray(data) ? data : []);

  const iconMap = {PROFIT:"💰",INFO:"ℹ️",WARN:"⚠️",ALERT:"🚨"};
  const colorMap = {PROFIT:"profit",INFO:"",WARN:"warn",ALERT:"loss"};

  el.innerHTML = `
    <div class="fade-in">
      <p class="section-title">${items.length} Events</p>
      <ul style="list-style:none;padding:0">
        ${items.map(n => `
          <li class="item-card" style="flex-direction:column;align-items:stretch">
            <div style="display:flex;gap:10px;align-items:flex-start">
              <span style="font-size:18px">${iconMap[n.level]??""}</span>
              <div style="flex:1">
                <div class="item-name ${colorMap[n.level]??""}">${esc(n.message)}</div>
                ${n.detail ? `<div class="item-meta">${esc(n.detail)}</div>` : ""}
                <div class="item-meta" style="font-size:11px">${(n.timestamp??"").slice(0,16).replace("T"," ")}</div>
              </div>
            </div>
          </li>`).join("")}
      </ul>
    </div>
  `;

  // Clear badge
  document.getElementById("notifBadge").classList.add("hidden");
}

// ── MODULE 08: Safe Control ────────────────────────────────────────────────
function renderSafe() {
  const el   = document.getElementById("screen-safe");
  const mode = localStorage.getItem("system_mode") || "FULL_AUTONOMY";
  const modeColor = mode === "FULL_AUTONOMY" ? "var(--green)" : "var(--red)";

  el.innerHTML = `
    <div class="fade-in">
      <div class="safe-mode-card" style="border-color:${modeColor}">
        <div class="safe-mode-label">SYSTEM MODE</div>
        <div class="safe-mode-value" style="color:${modeColor}">${mode.replace("_"," ")}</div>
      </div>

      <button class="btn btn-orange" onclick="activateSafeMode()">⏸ Activate Safe Mode</button>
      <button class="btn btn-green"  onclick="resumeOperations()">▶ Resume Full Autonomy</button>
      <hr class="safe-divider">
      <button class="btn btn-red"    onclick="emergencyShutdown()">🛑 Emergency Shutdown</button>
      <p style="font-size:12px;color:var(--grey);text-align:center;margin-top:8px">
        ⚠️ Shutdown closes all positions. Requires biometric confirmation.
      </p>
    </div>
  `;
}

async function activateSafeMode() {
  const ok = await requireBiometric();
  if (!ok) return;
  await apiFetch("/api/v1/emergency/safe_mode", { method: "POST" });
  localStorage.setItem("system_mode", "SAFE_MODE");
  updateSystemStatus("SAFE_MODE");
  renderSafe();
  showToast("⏸ Safe mode activated");
}

async function resumeOperations() {
  await apiFetch("/api/v1/emergency/resume", { method: "POST" });
  localStorage.setItem("system_mode", "FULL_AUTONOMY");
  updateSystemStatus("FULL_AUTONOMY");
  renderSafe();
  showToast("▶ Operations resumed");
}

async function emergencyShutdown() {
  const ok = await requireBiometric();
  if (!ok) return;
  await apiFetch("/api/v1/emergency/shutdown", { method: "POST" });
  localStorage.setItem("system_mode", "SHUTDOWN");
  updateSystemStatus("SHUTDOWN");
  renderSafe();
  showToast("🛑 Emergency shutdown initiated");
  window.dispatchEvent(new CustomEvent("lennit:shutdown"));
}

function updateSystemStatus(mode) {
  const el  = document.getElementById("systemStatus");
  const map = {
    FULL_AUTONOMY: ["status-ok",       "● ACTIVE"],
    SAFE_MODE:     ["status-safe",     "⏸ SAFE"],
    SHUTDOWN:      ["status-shutdown", "🛑 SHUTDOWN"],
    DEGRADED:      ["status-safe",     "⚠ DEGRADED"],
  };
  const [cls, label] = map[mode] ?? map.FULL_AUTONOMY;
  el.className  = `status-badge ${cls}`;
  el.textContent = label;
}

// ── MODULE 07: Settings ────────────────────────────────────────────────────
function renderSettings() {
  const el      = document.getElementById("screen-settings");
  const endpoint= localStorage.getItem("api_endpoint") || "";
  const apiKey  = localStorage.getItem("api_key") || "";
  const demo    = localStorage.getItem("demo_mode") === "true";

  el.innerHTML = `
    <div class="fade-in">
      <p class="section-title">Connection</p>
      <div class="card">
        <div class="form-group">
          <label class="form-label">API Endpoint</label>
          <input class="form-input" id="inputEndpoint" type="url" placeholder="http://your-vps:8000" value="${esc(endpoint)}">
        </div>
        <div class="form-group">
          <label class="form-label">API Key</label>
          <input class="form-input" id="inputApiKey" type="password" placeholder="••••••••" value="${esc(apiKey)}">
        </div>
        <button class="btn btn-copper" onclick="saveConnection()">💾 Save Connection</button>
      </div>

      <p class="section-title">Mode</p>
      <div class="card">
        <div class="toggle-row">
          <span class="toggle-label">Demo Mode (offline)</span>
          <label class="toggle-switch">
            <input type="checkbox" id="toggleDemo" ${demo?"checked":""} onchange="saveDemoMode(this.checked)">
            <span class="toggle-slider"></span>
          </label>
        </div>
      </div>

      <p class="section-title">Platform Info</p>
      <div class="card">
        <div class="item-meta">LENNITS CRYPTOLYZER v2.0.0</div>
        <div class="item-meta">by LENNIT_SUITE TECHNOLOGIES</div>
        <div class="item-meta">Target: Samsung S20 FE · Android 13</div>
        <div class="item-meta">Architecture: 20-Module Enterprise PWA + Native Android</div>
      </div>
    </div>
  `;
}

function saveConnection() {
  const endpoint = document.getElementById("inputEndpoint").value.trim();
  const apiKey   = document.getElementById("inputApiKey").value.trim();
  localStorage.setItem("api_endpoint", endpoint);
  localStorage.setItem("api_key", apiKey);
  API_BASE = endpoint;
  WS_URL   = endpoint.replace(/^http/, "ws") + "/ws/metrics";

  // Sync to Android bridge if available
  if (typeof LennitBridge !== "undefined") {
    LennitBridge.saveSetting("api_endpoint", endpoint);
    LennitBridge.saveSetting("api_key", apiKey);
  }

  connectWebSocket();
  showToast("✅ Connection saved");
}

function saveDemoMode(val) {
  localStorage.setItem("demo_mode", String(val));
}

// ── WebSocket — live metrics ────────────────────────────────────────────────
function connectWebSocket() {
  if (!API_BASE || ws?.readyState === WebSocket.OPEN) return;
  ws = new WebSocket(WS_URL);

  ws.onopen    = () => { wsRetries = 0; console.log("[WS] connected"); };
  ws.onmessage = (e) => {
    try {
      const d = JSON.parse(e.data);
      if (d.type === "metrics") handleLiveMetrics(d);
    } catch {}
  };
  ws.onclose   = () => {
    const delay = Math.min(30000, 1000 * 2 ** wsRetries++);
    setTimeout(connectWebSocket, delay);
  };
  ws.onerror   = () => ws.close();
}

function handleLiveMetrics(d) {
  const el = document.getElementById("totalPortfolio");
  if (el) el.textContent = `$${d.portfolio_usd?.toLocaleString("en-US",{minimumFractionDigits:2})}`;
}

// ── Biometric Auth (Android bridge) ───────────────────────────────────────
function requireBiometric() {
  if (typeof LennitBridge !== "undefined") {
    return new Promise(resolve => {
      window._biometricCb = (r) => resolve(r.success);
      LennitBridge.triggerBiometricAuth("window._biometricCb");
    });
  }
  return Promise.resolve(confirm("Confirm action (biometric not available in browser)"));
}

// ── Mock Data ──────────────────────────────────────────────────────────────
function getMockData(path) {
  const MOCK = {
    "/api/v1/dashboard": {
      total_portfolio_usd: "$155,000.00", pnl_24h_usd: "+$2,340.00",
      pnl_24h_pct: "+1.53%", active_agents: 20, total_agents: 20,
      system_status: "OK", uptime_seconds: 86400,
    },
    "/api/v1/portfolio/metrics": {
      total_value_usd:155000, allocated_usd:120000, available_usd:35000,
      daily_pnl:2340, weekly_pnl:11200, monthly_pnl:42000,
      drawdown_pct:-3.2, sharpe_ratio:2.4,
    },
    "/api/v1/agents": [
      ...[...Array(5)].map((_,i) => ({id:`Lennit_0${i+1}`,name:`Agent 0${i+1}`,status:"RUNNING",role:"ARBITRAGE",profit_usd:(i+1)*12.5,last_action:"scanning_spreads"})),
      ...[...Array(10)].map((_,i) => ({id:`Lennit_${(i+6).toString().padStart(2,"0")}`,name:`Agent ${(i+6).toString().padStart(2,"0")}`,status:"RUNNING",role:"HIMAP",profit_usd:(i+6)*12.5,last_action:"routing_cross_chain"})),
      ...[...Array(5)].map((_,i) => ({id:`Lennit_${(i+16).toString().padStart(2,"0")}`,name:`Agent ${(i+16).toString().padStart(2,"0")}`,status:"RUNNING",role:"DARKFOREST",profit_usd:(i+16)*12.5,last_action:"mempool_clear"})),
    ],
    "/api/v1/vault": [
      {symbol:"BTC",name:"Bitcoin",amount:1.234,usd_value:85000,chain:"BTC"},
      {symbol:"ETH",name:"Ethereum",amount:12.5,usd_value:42000,chain:"ETH",apy:4.2},
      {symbol:"SOL",name:"Solana",amount:350,usd_value:14000,chain:"SOL",apy:7.1},
      {symbol:"MATIC",name:"Polygon",amount:8000,usd_value:5600,chain:"POLYGON",apy:5.8},
      {symbol:"BNB",name:"BNB Chain",amount:22,usd_value:8400,chain:"BSC",apy:6.3},
    ],
    "/api/v1/strategies": [
      {id:"1",name:"BTC Perp Tri-Arb",type:"ARBITRAGE",mode:"LIVE",allocated_usd:30000,pnl_24h:1200,win_rate:0.68},
      {id:"2",name:"ETH/USDC LP Farming",type:"YIELD",mode:"LIVE",allocated_usd:25000,pnl_24h:320,win_rate:0.92},
      {id:"3",name:"Retroactive Airdrops",type:"AIRDROP",mode:"SHADOW",allocated_usd:5000,pnl_24h:150,win_rate:0.45},
      {id:"4",name:"Cross-chain Flash Arb",type:"MEV",mode:"LIVE",allocated_usd:20000,pnl_24h:870,win_rate:0.71},
      {id:"5",name:"MEV Sandwich Guard",type:"MEV",mode:"LIVE",allocated_usd:15000,pnl_24h:500,win_rate:0.78},
    ],
    "/api/v1/notifications?limit=50": [
      {id:"1",timestamp:"2026-04-03T08:00:00Z",level:"PROFIT",message:"BTC Tri-Arb: +$1,200",detail:"Spread: 0.42% | Gas: $12"},
      {id:"2",timestamp:"2026-04-03T07:45:00Z",level:"PROFIT",message:"ETH LP yield: +0.15 ETH",detail:"Pool: Uniswap V3 | APY: 4.2%"},
      {id:"3",timestamp:"2026-04-03T07:30:00Z",level:"INFO",message:"AlphaGrid: 20/20 active",detail:"Uptime: 24h 0m"},
      {id:"4",timestamp:"2026-04-03T07:15:00Z",level:"WARN",message:"Gas spike on ETH mainnet",detail:"65 gwei — above 50 gwei threshold"},
      {id:"5",timestamp:"2026-04-03T07:00:00Z",level:"PROFIT",message:"Cross-chain flash arb: +$870",detail:"ETH → BSC → POLYGON 3-hop"},
      {id:"6",timestamp:"2026-04-03T06:45:00Z",level:"ALERT",message:"Drawdown warning: -3.2%",detail:"Max allowed: -5.0%"},
    ],
  };
  return Promise.resolve(MOCK[path] ?? {});
}

// ── Utilities ──────────────────────────────────────────────────────────────
function esc(str) {
  return String(str ?? "")
    .replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;")
    .replace(/"/g,"&quot;").replace(/'/g,"&#39;");
}

function renderError(screenId, msg) {
  const el = document.getElementById(screenId);
  if (el) el.innerHTML = `<div class="card"><p class="loss">Error: ${esc(msg)}</p></div>`;
}

let _toastTimer;
function showToast(msg) {
  let t = document.getElementById("toast");
  if (!t) {
    t = document.createElement("div");
    t.id = "toast";
    t.style.cssText = `
      position:fixed;bottom:calc(var(--nav-h) + 12px);left:50%;transform:translateX(-50%);
      background:var(--surface-3);color:var(--white);padding:10px 20px;border-radius:20px;
      font-size:13px;z-index:9000;opacity:0;transition:opacity 0.3s ease;border:1px solid var(--surface-4);
    `;
    document.body.appendChild(t);
  }
  t.textContent = msg;
  t.style.opacity = "1";
  clearTimeout(_toastTimer);
  _toastTimer = setTimeout(() => { t.style.opacity = "0"; }, 2500);
}

// ── Bridge events ─────────────────────────────────────────────────────────
window.addEventListener("lennit:shutdown", () => updateSystemStatus("SHUTDOWN"));
window.addEventListener("lennit:safemode", () => updateSystemStatus("SAFE_MODE"));
