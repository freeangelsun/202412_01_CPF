/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmTraceBoostRequest {
  apiPath?: string;
  businessTransactionId?: string;
  durationMsGreaterThan?: number;
  failureCode?: string;
  logLevel?: string;
  policyId?: number;
  reason?: string;
  status?: string;
  transactionId?: string;
  ttlSeconds?: number;
}
