/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BatchJobDefinitionTransitionRequest {
  approvalRequestId?: string;
  expectedRowVersion: number;
  reason: string;
  targetState: string;
}
