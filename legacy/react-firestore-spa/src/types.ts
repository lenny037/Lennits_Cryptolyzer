export interface UserProfile {
  uid: string;
  email: string | null;
  displayName: string | null;
  photoURL: string | null;
  role: 'user' | 'admin' | 'agent';
  createdAt: string;
  updatedAt: string;
}

export interface WalletIdentity {
  address: string;
  chain: string;
  label: string;
  isOwner: boolean;
  lastActive: string;
}

export interface Agent {
  id: string;
  name: string;
  type: 'autonomous' | 'researcher' | 'executor' | 'arbitrage' | 'holder';
  status: 'idle' | 'running' | 'paused' | 'error';
  config: Record<string, any>;
  lastHeartbeat: string;
}

export interface TreasuryPosition {
  id: string;
  asset: string;
  amount: string;
  valueUsd: number;
  apy: number;
  platform: string;
  updatedAt: string;
}

export enum SecurityLevel {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}
