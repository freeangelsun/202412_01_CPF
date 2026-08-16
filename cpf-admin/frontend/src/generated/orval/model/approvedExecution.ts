/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface ApprovedExecution {
  approvalRequestId: number;
  approvedBy?: string;
  commandRequestId?: string;
  expectedVersion: number;
  reason?: string;
  requestedBy?: string;
}
