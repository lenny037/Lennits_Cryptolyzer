import { z } from 'zod';

export const AgentConfigSchema = z.object({
  operationalMode: z.enum(['SAFE', 'AUTONOMOUS']),
  resourceLimitUsd: z.number().min(0).max(1000000),
  maxSlippage: z.number().min(0.1).max(10.0),
  riskProfile: z.enum(['CONSERVATIVE', 'MODERATE', 'AGGRESSIVE']),
  profitThreshold: z.number().min(0).optional()
});

export type AgentConfig = z.infer<typeof AgentConfigSchema>;
