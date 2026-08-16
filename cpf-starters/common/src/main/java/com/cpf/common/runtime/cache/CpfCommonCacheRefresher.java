package com.cpf.common.runtime.cache;

/** Common Product Service의 local cache snapshot을 안전하게 갱신하는 내부 계약입니다. */
public interface CpfCommonCacheRefresher {
    void refresh(String cacheName);
    void refreshAll();
}
