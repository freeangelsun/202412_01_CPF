package com.cpf.core.api.cache;

import java.util.List;

/** Durable invalidation 원장과 checkpoint를 소유하는 Port입니다. */
public interface CpfCacheInvalidationPort {
    CpfCacheInvalidationEvent append(CpfCacheInvalidationEvent event);
    List<CpfCacheInvalidationEvent> loadAfter(long checkpoint, int limit);
    long checkpoint(String consumerId);
    void checkpoint(String consumerId, long eventId);
    long backlog(String consumerId);
}
