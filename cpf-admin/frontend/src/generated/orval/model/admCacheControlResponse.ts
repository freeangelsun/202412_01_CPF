import type { CpfCacheInvalidationEvent } from './cpfCacheInvalidationEvent';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmCacheControlResponse {
  accepted: boolean;
  affected: number;
  completedAt?: string;
  durableBacklog: number;
  durableEvent?: CpfCacheInvalidationEvent;
  message?: string;
  operation?: string;
  target?: string;
}
