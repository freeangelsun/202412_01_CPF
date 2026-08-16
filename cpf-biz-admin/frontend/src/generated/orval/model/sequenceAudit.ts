/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface SequenceAudit {
  afterValue: number;
  afterVersion: number;
  approvalId: number;
  beforeValue: number;
  beforeVersion: number;
  generatedAt?: string;
  generatedValue?: string;
  operationId?: string;
  operatorId?: string;
  reason?: string;
}
