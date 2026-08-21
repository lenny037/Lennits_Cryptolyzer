import React, { useState, useEffect } from 'react';
import { 
  Activity, 
  Shield, 
  Cpu, 
  Database, 
  Zap, 
  Terminal, 
  Lock, 
  ChevronRight,
  AlertTriangle,
  RefreshCw,
  Globe,
  PieChart
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { cn } from './lib/utils';
import { LennitLogo } from './components/LennitLogo';
import AgentsPage from './components/AgentsPage';

export default function App() {
  const [isLoaded, setIsLoaded] = useState(false);
  const [activeTab, setActiveTab] = useState('dashboard');

  const [kpis, setKpis] = useState<any>(null);
  const [globalLogs, setGlobalLogs] = useState<any[]>([]);

  useEffect(() => {
    setIsLoaded(true);
    fetch('/api/analytics/kpis')
      .then(res => res.json())
      .then(data => setKpis(data))
      .catch(err => console.error("KPI Fetch Error:", err));

    fetchLogs();
    const interval = setInterval(fetchLogs, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchLogs = async () => {
     try {
       const res = await fetch('/api/logs');
       const data = await res.json();
       setGlobalLogs(data);
     } catch (err) {
       console.error("Log Fetch Error:", err);
     }
  };

  return (
    <div className="min-h-screen flex text-slate-100 font-sans selection:bg-brand-primary/20 bg-[#0D0D11]">
      {/* Sidebar */}
      <aside className="w-72 glass-morphism border-r border-white/5 flex flex-col p-6 z-20">
        <div className="flex flex-col gap-1 mb-10">
          <LennitLogo mode="dark" className="w-full h-auto -ml-4" />
          <div className="h-px w-full bg-gradient-to-r from-brand-primary/50 to-transparent mt-2" />
        </div>

        <nav className="flex-1 space-y-2">
          <NavItem 
            icon={<PieChart size={18} />} 
            label="Overview" 
            active={activeTab === 'dashboard'} 
            onClick={() => setActiveTab('dashboard')} 
          />
          <NavItem 
            icon={<Cpu size={18} />} 
            label="Autonomous Agents" 
            active={activeTab === 'agents'} 
            onClick={() => setActiveTab('agents')} 
          />
          <NavItem 
            icon={<Activity size={18} />} 
            label="Live MEV Ops" 
            active={activeTab === 'mev'} 
            onClick={() => setActiveTab('mev')} 
          />
          <NavItem 
            icon={<Globe size={18} />} 
            label="Chain Intelligence" 
            active={activeTab === 'chain'} 
            onClick={() => setActiveTab('chain')} 
          />
          <NavItem 
            icon={<Shield size={18} />} 
            label="Security Hub" 
            active={activeTab === 'security'} 
            onClick={() => setActiveTab('security')} 
          />
        </nav>

        <div className="mt-auto pt-6 border-t border-white/5">
          <div className="flex items-center gap-3 p-3 rounded-xl bg-white/5">
            <div className="w-8 h-8 rounded-lg bg-brand-primary/10 flex items-center justify-center">
              <Lock className="text-brand-primary" size={14} />
            </div>
            <div className="text-xs uppercase tracking-widest font-bold text-slate-400">
              Safe Mode Active
            </div>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 relative overflow-hidden flex flex-col">
        {/* Header */}
        <header className="h-16 border-b border-white/5 flex items-center justify-between px-8 glass-morphism z-10">
          <div className="flex items-center gap-4 text-slate-400 text-sm font-mono tracking-tight">
            <span className="text-brand-primary">CORE</span>
            <ChevronRight size={14} />
            <span className="text-slate-100 font-medium uppercase">{activeTab}</span>
          </div>
          
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full bg-brand-primary animate-pulse" />
              <span className="text-xs font-mono text-brand-primary uppercase tracking-tighter">Live Relay Connected</span>
            </div>
          </div>
        </header>

        {/* Dynamic Content */}
        <div className="flex-1 p-8 overflow-y-auto bg-[radial-gradient(circle_at_50%_-20%,rgba(0,234,175,0.05),transparent_50%)]">
          <AnimatePresence mode="wait">
            <motion.div
              key={activeTab}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              transition={{ duration: 0.2 }}
              className="max-w-7xl mx-auto space-y-8"
            >
              {activeTab === 'dashboard' && <OverviewGrid kpis={kpis} logs={globalLogs} />}
              {activeTab === 'agents' && <AgentsPage />}
              {activeTab !== 'dashboard' && activeTab !== 'agents' && (
                <div className="h-[60vh] flex flex-col items-center justify-center space-y-4 text-center">
                  <div className="w-16 h-16 rounded-3xl bg-slate-900 border border-white/10 flex items-center justify-center mb-4">
                    <RefreshCw className="text-brand-primary animate-spin-slow" size={32} />
                  </div>
                  <h2 className="text-2xl font-bold font-sans tracking-tight">Lennits_Cryptolyzer Operations</h2>
                  <p className="text-slate-400 max-w-md">Phase 05: Canonicalization. Reconstructing authoritative module boundaries for {activeTab}.</p>
                </div>
              )}
            </motion.div>
          </AnimatePresence>
        </div>
      </main>
    </div>
  );
}

function NavItem({ icon, label, active, onClick }: { icon: React.ReactNode, label: string, active?: boolean, onClick: () => void }) {
  return (
    <button 
      onClick={onClick}
      className={cn(
        "w-full flex items-center gap-4 py-3 px-4 rounded-xl transition-all duration-200 group text-sm font-medium",
        active 
          ? "bg-brand-primary/10 text-brand-primary" 
          : "text-slate-400 hover:text-slate-100 hover:bg-white/5"
      )}
    >
      <span className={cn("transition-colors", active ? "text-brand-primary" : "group-hover:text-brand-primary")}>
        {icon}
      </span>
      {label}
    </button>
  );
}

function OverviewGrid({ kpis, logs }: { kpis: any, logs: any[] }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {/* Stats Widget */}
      <Widget title="Total Value Vaulted" className="col-span-1">
        <div className="flex flex-col">
          <div className="text-3xl font-bold tracking-tight text-glow text-white">
            ${kpis?.totalValueOptimized?.toLocaleString() || "1,242,506.84"}
          </div>
          <div className="text-xs text-brand-primary flex items-center gap-1 mt-2 font-mono uppercase tracking-widest">
            <ChevronRight size={10} className="-rotate-90" /> +12.4% APY Optimized
          </div>
        </div>
      </Widget>

      {/* Security Status */}
      <Widget title="Security Oracle" className="col-span-1">
        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <div className="text-lg font-semibold flex items-center gap-2">
              <Shield className={cn(
                kpis?.securityRiskAverage < 50 ? "text-brand-primary" : "text-red-500"
              )} size={18} />
              {kpis?.securityRiskAverage < 50 ? "SAFE" : "ALERT"}
            </div>
            <div className="text-xs text-slate-400 font-mono">Risk Engine Score: {kpis?.securityRiskAverage?.toFixed(1) || "12.0"}</div>
          </div>
          <div className="w-12 h-12 rounded-full border-2 border-brand-primary/20 flex items-center justify-center">
            <div className="w-8 h-8 rounded-full bg-brand-primary/10 border border-brand-primary/50 flex items-center justify-center">
              <div className="w-2 h-2 rounded-full bg-brand-primary animate-ping" />
            </div>
          </div>
        </div>
      </Widget>

      {/* System Health */}
      <Widget title="Infrastructure Health" className="col-span-1">
        <div className="grid grid-cols-2 gap-4">
          <HealthLine label="M01 Orchestration" status="online" />
          <HealthLine label="M14 Data Pipeline" status="online" />
          <HealthLine label="M16 Memory Node" status="syncing" />
          <HealthLine label="M06 MEV Engine" status="online" />
        </div>
      </Widget>

      {/* Terminal Widget */}
      <div className="col-span-1 lg:col-span-2 glass-morphism rounded-2xl p-6 relative overflow-hidden">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2 text-xs font-bold text-slate-500 uppercase tracking-widest">
            <Terminal size={14} /> Agent Execution Logs
          </div>
          <div className="flex items-center gap-2">
             <div className="w-1 h-1 rounded-full bg-brand-primary animate-pulse" />
             <span className="text-[10px] font-mono text-brand-primary uppercase">Live Sync</span>
          </div>
        </div>
        <div className="space-y-2 font-mono text-[11px] h-40 overflow-y-auto custom-scrollbar pr-2">
          {logs.length > 0 ? (
            logs.map((log) => (
              <LogLine 
                key={log.id} 
                time={new Date(log.createdAt).toLocaleTimeString()} 
                module={log.agentId ? log.agentId.slice(0, 3).toUpperCase() : (log.tags?.[0] || 'SYS').toUpperCase()} 
                msg={log.content} 
              />
            ))
          ) : (
            <div className="h-full flex items-center justify-center opacity-20">
               Initializing secure log stream...
            </div>
          )}
          <div className="animate-pulse flex items-center gap-2">
            <span className="text-slate-700">_</span>
          </div>
        </div>
      </div>

      {/* Signal Card */}
      <Widget title="Market Anomaly" className="border-brand-accent/30 bg-brand-accent/5">
        <div className="flex items-start gap-4">
          <div className="w-10 h-10 rounded-xl bg-brand-accent/20 flex items-center justify-center shrink-0">
            <AlertTriangle className="text-brand-accent" size={20} />
          </div>
          <div>
            <div className="text-sm font-bold text-white mb-1">M05 Warning: Prediction Drift</div>
            <p className="text-xs text-slate-400 leading-relaxed">System has detected a variance in social sentiment vs liquidity depth on ETH-BASE bridge routes.</p>
          </div>
        </div>
      </Widget>
    </div>
  );
}

function Widget({ title, children, className }: { title: string, children: React.ReactNode, className?: string }) {
  return (
    <div className={cn("glass-morphism rounded-2xl p-6 flex flex-col", className)}>
      <div className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-4 flex items-center gap-2">
         {title}
      </div>
      <div className="flex-1">
        {children}
      </div>
    </div>
  );
}

function HealthLine({ label, status }: { label: string, status: 'online' | 'offline' | 'syncing' }) {
  const statusColor = {
    online: 'bg-brand-primary',
    offline: 'bg-red-500',
    syncing: 'bg-brand-secondary'
  };
  
  return (
    <div className="flex items-center justify-between">
      <span className="text-[10px] font-medium text-slate-400 truncate">{label}</span>
      <div className={cn("w-1.5 h-1.5 rounded-full shadow-lg", statusColor[status], status === 'syncing' ? 'animate-pulse' : '')} />
    </div>
  );
}

const LogLine: React.FC<{ time: string, module: string, msg: string }> = ({ time, module, msg }) => {
  return (
    <div className="flex items-start gap-3">
      <span className="text-slate-600 shrink-0">{time}</span>
      <span className="text-brand-primary shrink-0">[{module}]</span>
      <span className="text-slate-300 break-all">{msg}</span>
    </div>
  );
}
