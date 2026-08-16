package com.cpf.platform.operations.observability.api.logging;

import java.util.Map;

/**
 * Integration/Domain Call Consumer가 구조화 연계 로그를 남기는 공개 Port입니다.
 * 파일 경로·저장 방식·마스킹 구현은 Observability Owner 내부에 숨깁니다.
 */
@FunctionalInterface
public interface CpfIntegrationLogPort {
    void writeIntegration(String sourceModuleCode, String targetModuleCode, String direction,
            String httpMethod, String apiPath, Integer httpStatus, String status, Long durationMs,
            String failureCode, String failureMessage, Map<String, Object> attributes);
}
