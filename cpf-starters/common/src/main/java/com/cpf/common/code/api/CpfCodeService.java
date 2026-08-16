package com.cpf.common.code.api;

import java.util.List;
import java.util.Optional;

/** Customer Application이 직접 소비하는 Common Code Product API입니다. */
public interface CpfCodeService {
    List<CpfCode> values(String group);
    Optional<CpfCode> find(String group, String value);

    /** 반드시 존재해야 하는 Code를 반환하며 누락 시 명확하게 실패합니다. */
    default CpfCode required(String group, String value) {
        return find(group, value).orElseThrow(() ->
                new java.util.NoSuchElementException("CPF Common code not found: group=" + group + ", value=" + value));
    }

    /**
     * @deprecated 업무 Source의 Golden Path가 아닙니다. 운영 Cache 갱신은
     * {@link com.cpf.common.message.api.CpfCommonCatalogManagementService#refreshCaches(String, String)}를 사용합니다.
     */
    @Deprecated(forRemoval = false)
    void refresh();
}
