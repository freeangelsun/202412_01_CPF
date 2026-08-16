import type { SequenceAudit } from './sequenceAudit';
import type { SequenceState } from './sequenceState';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface SequenceResult {
  audit?: SequenceAudit;
  replay: boolean;
  state?: SequenceState;
  value?: string;
}
