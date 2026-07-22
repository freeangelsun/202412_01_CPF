package com.cpf.core.common.logging;

import com.cpf.core.common.exception.CpfFrameworkException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 동적 로그 레벨 규칙의 필수 감사 정보와 런타임 등록 동작을 검증합니다. */
class DynamicTransactionLogLevelServiceTest {

    /** 감사 사유가 없는 규칙은 ADM 외 호출 경로에서도 등록되지 않는지 확인합니다. */
    @Test
    void rejectsRuleWithoutAuditReason() {
        DynamicLogLevelRequest request = new DynamicLogLevelRequest();
        request.setTransactionId("20260722120000000CPFcpfAP010000001");

        assertThatThrownBy(() -> new DynamicTransactionLogLevelService().register(request))
                .isInstanceOf(CpfFrameworkException.class)
                .hasMessageContaining("감사 사유");
    }

    /** 유효한 규칙의 사유 공백을 정리하고 TTL과 요청자를 보존하는지 확인합니다. */
    @Test
    void registersRuleWithNormalizedAuditMetadata() {
        DynamicLogLevelRequest request = new DynamicLogLevelRequest();
        request.setBusinessTransactionId("order-20260722-1");
        request.setModuleId("ref");
        request.setLogLevel(CpfLogLevel.TRACE);
        request.setTtl(Duration.ofMinutes(5));
        request.setReason("  장애 원인 분석  ");
        request.setRequestUser("operator01");

        DynamicLogLevelRule rule = new DynamicTransactionLogLevelService().register(request);

        assertThat(rule.businessTransactionId()).isEqualTo("ORDER-20260722-1");
        assertThat(rule.moduleId()).isEqualTo("REF");
        assertThat(rule.reason()).isEqualTo("장애 원인 분석");
        assertThat(rule.createdBy()).isEqualTo("operator01");
        assertThat(rule.expiresAt()).isAfter(rule.createdAt());
    }
}
