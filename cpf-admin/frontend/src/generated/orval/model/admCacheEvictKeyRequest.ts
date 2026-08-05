/** Audited cache key eviction request. The authenticated operator is resolved from the server session. */
export interface AdmCacheEvictKeyRequest {
  tenantId?: string;
  namespace: string;
  key: string;
  version: number;
  reason: string;
}
