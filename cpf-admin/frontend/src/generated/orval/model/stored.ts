import type { CpfRuntimeHealth } from './cpfRuntimeHealth';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Stored {
  health?: CpfRuntimeHealth;
  reportedAt?: string;
}
