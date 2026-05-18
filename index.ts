/**
 * LENNIT_CRYPTOLYZER — Structured Logger
 * Module: Core Infrastructure
 * Requirement: RULE 06 — All critical systems must emit logs
 *
 * Provides structured JSON logging compatible with Google Cloud Logging.
 * All log entries include: module, severity, timestamp, trace, correlationId.
 */

import { getFirestore, FieldValue } from "firebase-admin/firestore";

export type LogSeverity =
  | "DEBUG"
  | "INFO"
  | "NOTICE"
  | "WARNING"
  | "ERROR"
  | "CRITICAL"
  | "ALERT"
  | "EMERGENCY";

export interface LogEntry {
  severity: LogSeverity;
  module: string;
  message: string;
  timestamp: string;
  correlationId?: string;
  traceId?: string;
  data?: Record<string, unknown>;
  error?: {
    message: string;
    stack?: string;
    code?: string;
  };
}

export interface AuditEntry extends LogEntry {
  auditType: "EXECUTION" | "RISK_GATE" | "EMERGENCY" | "CONFIG_CHANGE";
  executionId?: string;
  modulePath?: string;
  result?: "PASS" | "BLOCK" | "ABORT" | "SUCCESS" | "FAILURE";
  riskScore?: number;
  amountUsd?: number;
}

/**
 * Logger factory — creates a module-scoped logger.
 * All log entries are emitted as structured JSON to stdout (Cloud Logging ingests this).
 */
export function getLogger(module: string) {
  return {
    debug: (message: string, data?: Record<string, unknown>) =>
      emit({ severity: "DEBUG", module, message, data }),

    info: (message: string, data?: Record<string, unknown>) =>
      emit({ severity: "INFO", module, message, data }),

    notice: (message: string, data?: Record<string, unknown>) =>
      emit({ severity: "NOTICE", module, message, data }),

    warn: (message: string, data?: Record<string, unknown>) =>
      emit({ severity: "WARNING", module, message, data }),

    error: (message: string, error?: Error, data?: Record<string, unknown>) =>
      emit({
        severity: "ERROR",
        module,
        message,
        data,
        error: error
          ? { message: error.message, stack: error.stack }
          : undefined,
      }),

    critical: (message: string, error?: Error, data?: Record<string, unknown>) =>
      emit({
        severity: "CRITICAL",
        module,
        message,
        data,
        error: error
          ? { message: error.message, stack: error.stack }
          : undefined,
      }),

    /**
     * Audit log — writes to Firestore /telemetry/audit AND stdout.
     * Required for all financial operations.
     */
    audit: async (entry: Omit<AuditEntry, "module" | "timestamp" | "severity">) => {
      const fullEntry: AuditEntry = {
        severity: "NOTICE",
        module,
        timestamp: new Date().toISOString(),
        ...entry,
      };
      emit(fullEntry);
      try {
        const db = getFirestore();
        await db.collection("telemetry").doc("audit").collection("entries").add({
          ...fullEntry,
          createdAt: FieldValue.serverTimestamp(),
        });
      } catch (err) {
        // Never let audit logging failure break execution
        emit({
          severity: "WARNING",
          module: "core/logger",
          message: "Audit Firestore write failed (stdout-only fallback)",
          timestamp: new Date().toISOString(),
          error: { message: String(err) },
        });
      }
    },
  };
}

function emit(entry: Omit<LogEntry, "timestamp"> & { timestamp?: string }): void {
  const structured: LogEntry = {
    timestamp: new Date().toISOString(),
    ...entry,
  };
  // Cloud Logging ingests structured JSON from stdout
  const severity = entry.severity ?? "INFO";
  const output = JSON.stringify({ ...structured, severity });

  if (severity === "ERROR" || severity === "CRITICAL" || severity === "ALERT" || severity === "EMERGENCY") {
    process.stderr.write(output + "\n");
  } else {
    process.stdout.write(output + "\n");
  }
}
