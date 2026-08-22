package com.cpf.core.api.error;

import java.util.Locale;
import java.util.Map;

/**
 * Error reference를 기술중립 오류 결과로 해석하는 Public SPI입니다.
 * cpf-common은 DB 기반 Catalog Provider를 제공하며, Capability 경계는 이 SPI만 소비합니다.
 */
public interface CpfErrorCatalogResolver {
    CpfResolvedErrorView resolve(
            String errorReference,
            CpfErrorDefinition fallback,
            Locale locale,
            Map<String, Object> arguments);
}
