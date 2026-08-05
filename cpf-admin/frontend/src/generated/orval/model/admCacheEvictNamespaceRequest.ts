/** Audited cache namespace eviction input. The authenticated operator is server-derived. */
export interface AdmCacheEvictNamespaceRequest { tenantId?: string; namespace: string; version: number; reason: string; }
