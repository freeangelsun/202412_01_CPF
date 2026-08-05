/** Audited cache namespace eviction request. The authenticated operator is resolved from the server session. */
export interface AdmCacheEvictNamespaceRequest {
  tenantId?: string;
  namespace: string;
  version: number;
  reason: string;
}
