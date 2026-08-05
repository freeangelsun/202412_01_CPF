/** ADM reliability action. The authenticated operator is resolved from the server session; requestUser is not accepted. */
export interface AdmReliabilityActionRequest {
  targetStatus?: string;
  reason: string;
}
