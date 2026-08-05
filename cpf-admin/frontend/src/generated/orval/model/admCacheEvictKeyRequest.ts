/** Audited cache key eviction input. The authenticated operator is server-derived. */
export interface AdmCacheEvictKeyRequest { tenantId?: string; namespace: string; key: string; version: number; reason: string; }
