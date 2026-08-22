package com.cpf.common.message.api;

import com.cpf.core.api.error.CpfErrorDefinition;
import java.util.Locale;
import java.util.Map;

/**
 * DB 기반 Response Code → Message Code → Locale Message 해석 계약입니다.
 * HTTP/Batch/Broker 상태 매핑은 이 계약에 포함하지 않고 각 Capability Owner가 담당합니다.
 */
public interface CpfErrorCatalogResolver extends com.cpf.core.api.error.CpfErrorCatalogResolver {
    @Override
    CpfResolvedError resolve(
            String errorReference,
            CpfErrorDefinition fallback,
            Locale locale,
            Map<String, Object> arguments);
}
