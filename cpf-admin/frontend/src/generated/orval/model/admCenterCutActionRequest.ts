/** Approved Center-Cut execution-scope recovery command. The authenticated executor is resolved from the server session. */
export interface AdmCenterCutActionRequest {
  reason: string;
  approvalRequestId: string;
  idempotencyKey: string;
}
