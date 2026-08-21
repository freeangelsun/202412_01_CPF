/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface RetentionPolicySaveRequest {
  action: string;
  chunkSize: number;
  enabled: boolean;
  leaseSeconds: number;
  legalHold: boolean;
  maintenanceEnd?: string;
  maintenanceStart?: string;
  maxRowsPerRun: number;
  maxRuntimeSeconds: number;
  nextRunAt?: string;
  policyId: string;
  policyVersion: number;
  reason: string;
  retentionDays: number;
  rowVersion: number;
  scheduleExpression?: string;
  target: string;
  throttleMillis: number;
}
