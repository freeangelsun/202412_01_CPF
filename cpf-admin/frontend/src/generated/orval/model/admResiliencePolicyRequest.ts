/** Resilience policy change request requiring a separate approval. */
export interface AdmResiliencePolicyRequest {
  operationId: string;
  timeoutMs: number;
  maxAttempts: number;
  retryBackoffMs: number;
  circuitFailureThreshold: number;
  circuitOpenMs: number;
  bulkheadMaxConcurrent: number;
  rateLimitPermits: number;
  rateLimitWindowMs: number;
  idempotent: boolean;
  unknownResultReconcileEnabled: boolean;
  reason: string;
}
