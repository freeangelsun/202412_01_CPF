import type { SequenceAudit } from './sequenceAudit';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface SequenceState {
  businessDate?: string;
  currentValue: number;
  lastAudit?: SequenceAudit;
  lastGeneratedValue?: string;
  lastOperationId?: string;
  lastRequestHash?: string;
  version: number;
}
