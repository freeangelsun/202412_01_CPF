package com.cpf.platform.operations.observability.api.logging;

import java.util.Map;

/**
 * 고객 업무 Source가 거래 Context와 자동 상관되는 구조화 로그를 남길 때 사용하는 단일 공개 API입니다.
 *
 * <p>업무/운영/보안/오류 로그를 기능별 Service로 쪼개지 않고 하나의 Logger에 모읍니다.
 * transactionId, executionId, segmentId, traceId, attempt는 Framework가 자동으로 붙이며
 * 전달 필드는 마스킹·길이 제한 후 기록됩니다. 위험 조치 감사는 일반 로그가 아니라
 * {@code @CpfAudit} / {@code CpfAuditSink}의 durable audit 계약을 사용해야 합니다.</p>
 */
public interface CpfStructuredLogger {
    /** 업무 상태·의미 변화를 기록합니다. */
    void business(String event, Map<String, ?> fields);

    /** 운영 상태·제어 결과를 기록합니다. */
    void operation(String event, Map<String, ?> fields);

    /** 인증·인가·정책 위반 등 보안 이벤트를 기록합니다. */
    void security(String event, Map<String, ?> fields);

    /** 업무 Source에서 분류가 필요한 오류를 안전한 필드와 함께 기록합니다. */
    void error(String event, Throwable error, Map<String, ?> fields);
}
