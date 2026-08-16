package com.cpf.data.cache.api;

/** 사용 가능한 Cache 값이 없을 때 Canonical 원본 저장소에서 값을 읽는 Loader 계약입니다. */
@FunctionalInterface
public interface CpfCacheLoader {
    CpfCacheValue load(CpfCacheKey key) throws Exception;
}
