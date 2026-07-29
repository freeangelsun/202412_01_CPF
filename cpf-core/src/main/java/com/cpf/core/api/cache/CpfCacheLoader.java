package com.cpf.core.api.cache;

/** Cache miss 시 원본 저장소에서 값을 읽는 Consumer callback입니다. */
@FunctionalInterface
public interface CpfCacheLoader {
    CpfCacheValue load(CpfCacheKey key) throws Exception;
}
