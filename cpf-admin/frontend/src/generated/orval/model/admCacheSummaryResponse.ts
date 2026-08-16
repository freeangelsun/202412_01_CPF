import type { CpfCacheHealth } from './cpfCacheHealth';
import type { CpfCacheMetricsSnapshot } from './cpfCacheMetricsSnapshot';
import type { DomainStatus } from './domainStatus';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmCacheSummaryResponse {
  available: boolean;
  domains?: Array<DomainStatus>;
  durableBacklog: number;
  message?: string;
  metrics?: CpfCacheMetricsSnapshot;
  providerHealth?: CpfCacheHealth;
}
