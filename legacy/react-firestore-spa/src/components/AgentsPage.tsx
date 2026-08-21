import React, { useState, useEffect } from 'react';
import { 
  Settings, 
  Play, 
  Pause, 
  Save, 
  Cpu, 
  ShieldAlert, 
  Target,
  Zap,
  TrendingUp,
  Sliders,
  RefreshCw,
  AlertCircle,
  Clock,
  Activity,
  Terminal,
  Trash2,
  X
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { cn } from '../lib/utils';
import { Agent } from '../types';
import type { SystemMemory } from '../modules/m16-memory/MemoryStore';

import { AgentConfigSchema } from '../lib/schemas';

import { 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer 
} from 'recharts';

export default function AgentsPage() {
  const [agents, setAgents] = useState<Agent[]>([]);
  const [selectedAgent, setSelectedAgent] = useState<Agent | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [filterType, setFilterType] = useState<string>('all');
  const [logs, setLogs] = useState<SystemMemory[]>([]);
  const [showStopConfirm, setShowStopConfirm] = useState(false);

  // Simulated chart data
  const performanceData = [
    { time: '00:00', value: 400 },
    { time: '04:00', value: 300 },
    { time: '08:00', value: 600 },
    { time: '12:00', value: 500 },
    { time: '16:00', value: 900 },
    { time: '20:00', value: 800 },
    { time: '23:59', value: 1100 },
  ];

  // Form State
  const [config, setConfig] = useState<any>({
    operationalMode: 'SAFE',
    resourceLimitUsd: 100,
    maxSlippage: 0.5,
    riskProfile: 'MODERATE'
  });

  useEffect(() => {
    fetchAgents();
  }, []);

  useEffect(() => {
    if (selectedAgent) {
      setConfig({
        operationalMode: selectedAgent.config?.operationalMode || 'SAFE',
        resourceLimitUsd: selectedAgent.config?.resourceLimitUsd || 100,
        maxSlippage: selectedAgent.config?.maxSlippage || 0.5,
        riskProfile: selectedAgent.config?.riskProfile || 'MODERATE'
      });
      fetchLogs(selectedAgent.id);
    }
  }, [selectedAgent]);

  useEffect(() => {
    if (selectedAgent) {
      const interval = setInterval(() => {
        fetchLogs(selectedAgent.id);
      }, 5000);
      return () => clearInterval(interval);
    }
  }, [selectedAgent]);

  const fetchAgents = async () => {
    setIsLoading(true);
    try {
      const res = await fetch('/api/agents');
      const data = await res.json();
      setAgents(data);
      if (data.length > 0 && !selectedAgent) {
        setSelectedAgent(data[0]);
      }
    } catch (err) {
      console.error("Failed to fetch agents:", err);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchLogs = async (id: string) => {
    try {
      const res = await fetch(`/api/agents/${id}/logs`);
      const data = await res.json();
      setLogs(data);
    } catch (err) {
      console.error("Failed to fetch logs:", err);
    }
  };

  const [opportunities, setOpportunities] = useState<any[]>([]);

  useEffect(() => {
    const interval = setInterval(async () => {
      if (selectedAgent?.type === 'ARBITRAGE') {
        try {
          const res = await fetch('/api/mev/discovery');
          const data = await res.json();
          setOpportunities(data);
        } catch (err) {
          console.error("Discovery error:", err);
        }
      }
    }, 5000);
    return () => clearInterval(interval);
  }, [selectedAgent]);

  const handleStartAgent = async () => {
    if (!selectedAgent) return;
    try {
      const res = await fetch(`/api/agents/${selectedAgent.id}/start`, { method: 'POST' });
      if (res.ok) {
        fetchAgents();
      }
    } catch (err) {
      console.error("Start error:", err);
    }
  };

  const handleStopAgent = async () => {
    if (!selectedAgent) return;
    setShowStopConfirm(false);
    try {
      const res = await fetch(`/api/agents/${selectedAgent.id}/stop`, { method: 'POST' });
      if (res.ok) {
        fetchAgents();
      }
    } catch (err) {
      console.error("Stop error:", err);
    }
  };

  const handleSave = async () => {
    if (!selectedAgent) return;

    // Client-side validation
    const result = AgentConfigSchema.safeParse(config);
    if (!result.success) {
      console.error("Validation failed:", result.error);
      alert("Invalid configuration: " + result.error.issues.map(e => e.message).join(", "));
      return;
    }

    setIsSaving(true);
    try {
      const res = await fetch(`/api/agents/${selectedAgent.id}/config`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ config })
      });
      if (res.ok) {
        console.log("Config saved successfully");
        fetchAgents(); 
      }
    } catch (err) {
      console.error("Failed to save config:", err);
    } finally {
      setIsSaving(false);
    }
  };

  const filteredAgents = agents.filter(a => 
    filterType === 'all' ? true : a.type.toLowerCase() === filterType.toLowerCase()
  );

  const getStatusBadge = (status: string) => {
    const config: Record<string, { bg: string, text: string, icon: any, border: string, label: string }> = {
      running: { bg: 'bg-emerald-500/10', text: 'text-emerald-400', icon: Activity, border: 'border-emerald-500/20', label: 'ACTIVE' },
      idle: { bg: 'bg-slate-800', text: 'text-slate-400', icon: Pause, border: 'border-slate-700', label: 'READY' },
      paused: { bg: 'bg-amber-500/10', text: 'text-amber-400', icon: Clock, border: 'border-amber-500/20', label: 'SUSPENDED' },
      error: { bg: 'bg-rose-500/10', text: 'text-rose-400', icon: AlertCircle, border: 'border-rose-500/20', label: 'CRITICAL' }
    };
    const s = config[status as keyof typeof config] || config.idle;
    const Icon = s.icon;

    return (
      <span className={cn(
        "text-[9px] uppercase font-bold px-2 py-0.5 rounded-full flex items-center gap-1.5 border leading-none h-5",
        s.bg,
        s.text,
        s.border
      )}>
        <Icon size={10} className={status === 'running' ? "animate-pulse" : ""} />
        {s.label}
      </span>
    );
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-brand-primary"></div>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 h-full">
      {/* Agent List */}
      <div className="lg:col-span-4 space-y-6">
        <div className="space-y-4">
          <h3 className="text-sm font-bold text-slate-500 uppercase tracking-widest px-2">Active Fleet</h3>
          
          {/* Filters */}
          <div className="flex gap-2 px-2 overflow-x-auto pb-2 scrollbar-hide">
            {['all', 'arbitrage', 'holder'].map((type) => (
              <button
                key={type}
                onClick={() => setFilterType(type)}
                className={cn(
                  "px-3 py-1.5 rounded-lg text-[10px] font-bold uppercase transition-all whitespace-nowrap",
                  filterType === type 
                    ? "bg-brand-primary text-[#0D0D11]" 
                    : "bg-white/5 text-slate-400 hover:bg-white/10"
                )}
              >
                {type}
              </button>
            ))}
          </div>
        </div>

        <div className="space-y-3">
          {filteredAgents.map((agent) => (
            <button
              key={agent.id}
              onClick={() => setSelectedAgent(agent)}
              className={cn(
                "w-full text-left p-4 rounded-2xl transition-all border group",
                selectedAgent?.id === agent.id 
                  ? "glass-morphism border-brand-primary/50 ring-1 ring-brand-primary/20 bg-brand-primary/5 shadow-lg shadow-brand-primary/5" 
                  : "border-white/5 hover:bg-white/5"
              )}
            >
              <div className="flex items-center gap-3">
                <div className={cn(
                  "p-2.5 rounded-xl transition-all",
                  selectedAgent?.id === agent.id 
                    ? "bg-brand-primary/20 border border-brand-primary/30" 
                    : "bg-white/5 border border-white/5 group-hover:border-white/10"
                )}>
                  <Cpu 
                    size={20} 
                    className={cn(
                      "transition-colors",
                      selectedAgent?.id === agent.id ? "text-brand-primary" : "text-slate-500 group-hover:text-slate-300"
                    )} 
                  />
                </div>
                <div className="flex-1 overflow-hidden">
                  <div className="flex items-center justify-between mb-1">
                    <span className={cn(
                      "font-bold transition-colors truncate",
                      selectedAgent?.id === agent.id ? "text-slate-100" : "text-slate-400 group-hover:text-slate-200"
                    )}>{agent.name}</span>
                    {getStatusBadge(agent.status)}
                  </div>
                  <div className="flex items-center justify-between">
                    <div className="text-[9px] text-slate-500 font-mono uppercase tracking-wider">{agent.type} NODE</div>
                    <div className="text-[9px] text-slate-600 font-mono">ID: {agent.id.slice(0, 8)}...</div>
                  </div>
                </div>
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* Configuration Panel */}
      <div className="lg:col-span-8">
        <AnimatePresence mode="wait">
          {selectedAgent ? (
            <motion.div
              key={selectedAgent.id}
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              className="glass-morphism rounded-3xl p-8 space-y-8"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-2xl bg-brand-primary/10 flex items-center justify-center">
                    <Settings className="text-brand-primary" />
                  </div>
                  <div>
                    <h2 className="text-xl font-bold tracking-tight">{selectedAgent.name}</h2>
                    <p className="text-xs text-slate-500 font-mono">ID: {selectedAgent.id} • STATUS: {selectedAgent.status}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  {selectedAgent.status === 'running' ? (
                    <button
                      onClick={() => setShowStopConfirm(true)}
                      className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-500 text-sm font-bold hover:bg-rose-500/20 transition-all shadow-lg shadow-rose-500/5"
                    >
                      <Pause size={16} fill="currentColor" />
                      Suspend Execution
                    </button>
                  ) : (
                    <button
                      onClick={handleStartAgent}
                      className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-sm font-bold hover:bg-emerald-500/20 transition-all shadow-lg shadow-emerald-500/5"
                    >
                      <Play size={16} fill="currentColor" />
                      Initialize Agent
                    </button>
                  )}
                  <button
                    onClick={handleSave}
                    disabled={isSaving}
                    className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-brand-primary text-[#0D0D11] text-sm font-bold hover:shadow-xl hover:shadow-brand-primary/30 transition-all disabled:opacity-50"
                  >
                    {isSaving ? <RefreshCw className="animate-spin" size={16} /> : <Save size={16} />}
                    {isSaving ? 'Synchronizing...' : 'Save Config'}
                  </button>
                </div>
              </div>

              {/* Stop Confirmation Modal Overlay */}
              <AnimatePresence>
                {showStopConfirm && (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="fixed inset-0 z-50 flex items-center justify-center bg-[#0D0D11]/80 backdrop-blur-sm p-4"
                  >
                    <motion.div
                      initial={{ scale: 0.9, opacity: 0 }}
                      animate={{ scale: 1, opacity: 1 }}
                      exit={{ scale: 0.9, opacity: 0 }}
                      className="glass-morphism border-rose-500/30 p-8 rounded-3xl max-w-md w-full space-y-6"
                    >
                      <div className="flex items-center gap-4 text-rose-500">
                        <div className="p-3 rounded-2xl bg-rose-500/10">
                          <AlertCircle size={24} />
                        </div>
                        <h3 className="text-xl font-bold">Confirm Deactivation</h3>
                      </div>
                      <p className="text-slate-400 text-sm leading-relaxed">
                        Are you sure you want to stop <span className="text-slate-100 font-bold">{selectedAgent.name}</span>? 
                        Any pending autonomous discoveries or executions will be immediately halted.
                      </p>
                      <div className="flex gap-3 pt-4">
                        <button
                          onClick={() => setShowStopConfirm(false)}
                          className="flex-1 py-3 rounded-xl bg-white/5 border border-white/10 text-slate-300 font-bold hover:bg-white/10 transition-all"
                        >
                          Cancel
                        </button>
                        <button
                          onClick={handleStopAgent}
                          className="flex-1 py-3 rounded-xl bg-rose-500 text-white font-bold hover:bg-rose-600 transition-all shadow-lg shadow-rose-500/20"
                        >
                          Stop Agent
                        </button>
                      </div>
                    </motion.div>
                  </motion.div>
                )}
              </AnimatePresence>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {/* Mode Selector */}
                <div className="space-y-4">
                  <label className="text-[10px] font-bold text-slate-500 uppercase tracking-widest flex items-center gap-2">
                    <Zap size={14} className="text-brand-primary" /> Operational Mode
                  </label>
                  <div className="grid grid-cols-2 gap-3">
                    {['SAFE', 'AUTONOMOUS'].map((mode) => (
                      <button
                        key={mode}
                        onClick={() => setConfig({ ...config, operationalMode: mode })}
                        className={cn(
                          "py-3 px-4 rounded-xl text-xs font-bold transition-all border",
                          config.operationalMode === mode 
                            ? "bg-brand-primary/10 border-brand-primary text-brand-primary" 
                            : "border-white/10 text-slate-400 hover:border-white/20"
                        )}
                      >
                        {mode}
                      </button>
                    ))}
                  </div>
                  <p className="text-[10px] text-slate-500 leading-relaxed px-1 italic">
                    {config.operationalMode === 'SAFE' 
                      ? "Requires manual approval for all high-risk transactions." 
                      : "Direct execution without human intervention. Use with caution."}
                  </p>
                </div>

                {/* Risk Profile */}
                <div className="space-y-4">
                  <label className="text-[10px] font-bold text-slate-500 uppercase tracking-widest flex items-center gap-2">
                    <ShieldAlert size={14} className="text-brand-primary" /> Risk Profile
                  </label>
                  <div className="grid grid-cols-3 gap-2">
                    {['CONSERVATIVE', 'MODERATE', 'AGGRESSIVE'].map((level) => (
                      <button
                        key={level}
                        onClick={() => setConfig({ ...config, riskProfile: level })}
                        className={cn(
                          "py-2 px-1 rounded-lg text-[9px] font-bold transition-all border",
                          config.riskProfile === level 
                            ? "bg-brand-primary/10 border-brand-primary text-brand-primary" 
                            : "border-white/10 text-slate-500 hover:border-white/20"
                        )}
                      >
                        {level}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Resource Allocation */}
                <div className="space-y-4">
                  <label className="text-[10px] font-bold text-slate-500 uppercase tracking-widest flex items-center gap-2">
                    <TrendingUp size={14} className="text-brand-primary" /> Resource Limit (USD)
                  </label>
                  <div className="relative group">
                    <input
                      type="number"
                      value={config.resourceLimitUsd}
                      onChange={(e) => setConfig({ ...config, resourceLimitUsd: parseFloat(e.target.value) })}
                      className="w-full bg-slate-900 border border-white/10 rounded-xl px-4 py-3 text-sm focus:border-brand-primary/50 focus:ring-1 focus:ring-brand-primary/20 outline-none transition-all"
                    />
                    <div className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-600 text-[10px] font-mono">USD</div>
                  </div>
                </div>

                {/* Execution Thresholds */}
                <div className="space-y-4">
                  <label className="text-[10px] font-bold text-slate-500 uppercase tracking-widest flex items-center gap-2">
                    <Sliders size={14} className="text-brand-primary" /> Max Slippage (%)
                  </label>
                  <div className="flex items-center gap-4">
                    <input
                      type="range"
                      min="0.1"
                      max="5.0"
                      step="0.1"
                      value={config.maxSlippage}
                      onChange={(e) => setConfig({ ...config, maxSlippage: parseFloat(e.target.value) })}
                      className="flex-1 h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-brand-primary"
                    />
                    <span className="text-slate-100 font-mono text-xs w-10 text-right">{config.maxSlippage}%</span>
                  </div>
                </div>

                {/* Profit Threshold for Arbitrage */}
                {selectedAgent.type === 'arbitrage' && (
                  <div className="space-y-4">
                    <label className="text-[10px] font-bold text-slate-500 uppercase tracking-widest flex items-center gap-2">
                      <Target size={14} className="text-brand-primary" /> Min. Profit Threshold (USD)
                    </label>
                    <div className="relative">
                      <input
                        type="number"
                        value={config.profitThreshold || 0}
                        onChange={(e) => setConfig({ ...config, profitThreshold: parseFloat(e.target.value) })}
                        className="w-full bg-slate-900 border border-white/10 rounded-xl px-4 py-3 text-sm focus:border-brand-primary/50 focus:ring-1 focus:ring-brand-primary/20 outline-none transition-all"
                      />
                      <div className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-600 text-[10px] font-mono">USD</div>
                    </div>
                  </div>
                )}
              </div>
              
              <div className="pt-8 border-t border-white/5 space-y-6">
                <div className="flex items-center justify-between">
                  <h4 className="text-[10px] font-bold text-slate-500 uppercase tracking-widest flex items-center gap-2">
                    <Terminal size={14} className="text-brand-primary" /> Agent Execution Logs
                  </h4>
                  <div className="flex items-center gap-1.5">
                    <div className="w-1.5 h-1.5 rounded-full bg-brand-primary animate-pulse" />
                    <span className="text-[9px] font-bold text-brand-primary uppercase tracking-tighter">Real-time Stream</span>
                  </div>
                </div>

                <div className="bg-black/40 rounded-2xl border border-white/5 p-4 h-64 overflow-y-auto scrollbar-hide font-mono text-[11px] space-y-2">
                   {logs.length > 0 ? (
                     logs.map((log) => (
                       <div key={log.id} className="flex gap-4 border-l-2 border-white/5 pl-4 py-1 hover:bg-white/5 transition-colors group">
                          <span className="text-slate-600 whitespace-nowrap">[{new Date(log.createdAt).toLocaleTimeString()}]</span>
                          <span className={cn(
                            "font-bold uppercase w-20 whitespace-nowrap",
                            log.importance > 0.8 ? "text-rose-400" : log.importance > 0.4 ? "text-brand-primary" : "text-emerald-400"
                          )}>{log.type}</span>
                          <span className="text-slate-300 leading-relaxed break-words">{log.content}</span>
                       </div>
                     ))
                   ) : (
                     <div className="h-full flex flex-col items-center justify-center gap-3 opacity-30">
                        <Terminal size={32} />
                        <p>No execution logs available for this agent session.</p>
                     </div>
                   )}
                </div>
              </div>

              <div className="pt-8 border-t border-white/5 space-y-6">
                <div className="flex items-center justify-between">
                  <h4 className="text-[10px] font-bold text-slate-500 uppercase tracking-widest flex items-center gap-2">
                    <Target size={14} className="text-brand-primary" /> Autonomous Discovery Stream
                  </h4>
                  <div className="flex items-center gap-1.5">
                    <div className="w-1.5 h-1.5 rounded-full bg-brand-primary animate-pulse" />
                    <span className="text-[9px] font-bold text-brand-primary uppercase tracking-tighter">Live Engine</span>
                  </div>
                </div>

                {opportunities.length > 0 ? (
                  <div className="space-y-3">
                    {opportunities.map((opp: any) => (
                      <div key={opp.id} className="flex items-center justify-between p-4 rounded-2xl bg-white/5 border border-white/5 hover:border-white/10 transition-all group">
                        <div className="flex items-center gap-4">
                           <div className="text-xs font-mono text-slate-400 group-hover:text-slate-100 transition-colors">
                              {opp.path.join(' → ')}
                           </div>
                           <div className="text-[10px] text-slate-500 bg-white/5 px-2 py-0.5 rounded uppercase font-bold">
                              {opp.route}
                           </div>
                        </div>
                        <div className="text-right">
                           <div className="text-sm font-bold text-brand-primary">+${opp.expectedProfitUsd.toFixed(2)}</div>
                           <div className="text-[9px] text-slate-500 font-mono">EST. GAS: ${opp.estimatedGasUsd.toFixed(2)}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="h-24 flex flex-col items-center justify-center border border-dashed border-white/10 rounded-2xl gap-2">
                    <RefreshCw className="text-slate-600 animate-spin-slow" size={20} />
                    <span className="text-[10px] text-slate-500 font-mono">Scanning mempool for opportunities...</span>
                  </div>
                )}
              </div>
              
              <div className="pt-8 border-t border-white/5 space-y-6">
                <div className="flex items-center justify-between">
                  <h4 className="text-[10px] font-bold text-slate-500 uppercase tracking-widest flex items-center gap-2">
                    <Activity size={14} className="text-brand-primary" /> Performance Analytics
                  </h4>
                  <div className="text-[9px] font-mono text-slate-400">DELTA: +14.2% (24H)</div>
                </div>
                
                <div className="h-48 w-full bg-black/20 rounded-2xl p-4 border border-white/5">
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={performanceData}>
                      <defs>
                        <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#00EAAF" stopOpacity={0.3}/>
                          <stop offset="95%" stopColor="#00EAAF" stopOpacity={0}/>
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" vertical={false} />
                      <XAxis 
                        dataKey="time" 
                        stroke="#4b5563" 
                        fontSize={10} 
                        tickLine={false} 
                        axisLine={false} 
                      />
                      <YAxis hide />
                      <Tooltip 
                        contentStyle={{ backgroundColor: '#0D0D11', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px' }}
                        itemStyle={{ color: '#00EAAF', fontSize: '12px' }}
                      />
                      <Area 
                        type="monotone" 
                        dataKey="value" 
                        stroke="#00EAAF" 
                        fillOpacity={1} 
                        fill="url(#colorValue)" 
                        strokeWidth={2}
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              </div>

              <div className="pt-8 border-t border-white/5">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                   <div className="glass-morphism bg-white/5 rounded-2xl p-4 flex flex-col gap-2">
                      <div className="text-[9px] uppercase font-bold text-slate-500">Uptime Reliability</div>
                      <div className="text-lg font-bold text-slate-100">99.98%</div>
                   </div>
                   <div className="glass-morphism bg-white/5 rounded-2xl p-4 flex flex-col gap-2">
                      <div className="text-[9px] uppercase font-bold text-slate-500">Avg. Execution Speed</div>
                      <div className="text-lg font-bold text-slate-100">142ms</div>
                   </div>
                   <div className="glass-morphism bg-white/5 rounded-2xl p-4 flex flex-col gap-2">
                      <div className="text-[9px] uppercase font-bold text-slate-500">Simulations Passed</div>
                      <div className="text-lg font-bold text-slate-100">14,206</div>
                   </div>
                </div>
              </div>
            </motion.div>
          ) : (
            <div className="h-full flex items-center justify-center text-slate-500 text-sm">
              Select an agent to view and configure settings.
            </div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
