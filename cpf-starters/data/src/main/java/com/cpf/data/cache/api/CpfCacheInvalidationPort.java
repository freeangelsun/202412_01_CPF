package com.cpf.data.cache.api;

import java.util.List;

/**
 * Durable cache invalidation ledger/checkpoint/version-fence contract.
 *
 * <p>The version fence is scoped by consumer because each instance must prove that it has applied a
 * monotonic version independently. Implementations must make {@link #advanceVersion} monotonic and
 * safe under duplicate/concurrent calls.</p>
 */
/** CpfCacheInvalidationPort 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfCacheInvalidationPort {
    CpfCacheInvalidationEvent append(CpfCacheInvalidationEvent event);
    List<CpfCacheInvalidationEvent> loadAfter(long checkpoint, int limit);
    long checkpoint(String consumerId);
    void checkpoint(String consumerId, long eventId);
    long backlog(String consumerId);
    long version(String consumerId, String tenantId, String namespace, String cacheKey);
    void advanceVersion(String consumerId, String tenantId, String namespace, String cacheKey, long version);
}
