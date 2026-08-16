import type { Status } from './status';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Evaluation {
  reason?: string;
  status?: Status;
}
