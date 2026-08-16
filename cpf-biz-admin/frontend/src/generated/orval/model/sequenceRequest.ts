/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface SequenceRequest {
  approvalId: number;
  businessDate?: string;
  expectedVersion: number;
  operationId?: string;
  operatorId?: string;
  reason?: string;
  ruleCode?: string;
}
