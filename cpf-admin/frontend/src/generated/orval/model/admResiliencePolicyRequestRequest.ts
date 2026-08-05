/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmResiliencePolicyRequestRequest {
  bulkheadMaxConcurrent: number;
  circuitFailureThreshold: number;
  circuitOpenMs: number;
  idempotent: boolean;
  maxAttempts: number;
  operationId?: string;
  rateLimitPermits: number;
  rateLimitWindowMs: number;
  reason?: string;
  retryBackoffMs: number;
  timeoutMs: number;
  unknownResultReconcileEnabled: boolean;
}
