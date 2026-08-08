/** ADM reliability action. The authenticated operator is resolved from the server session. */
export interface AdmReliabilityActionRequest {
  targetStatus?: string;
  expectedVersion?: number;
  reason: string;
}
