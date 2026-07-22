package com.cpf.core.common.logging;

import com.cpf.core.common.logging.policy.LogPolicyDecision;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * Controller AOP에서 수집한 거래 로그를 비동기 이벤트 흐름으로 전달합니다.
 *
 * <p>로그 수집과 DB 저장을 분리해 Controller 응답 처리 코드를 단순하게 유지합니다.</p>
 */
@Getter
public class TransactionLogEvent extends ApplicationEvent {

    private final TransactionLogRecord record;
    private final Map<String, String> details;
    private final LogPolicyDecision logPolicy;

    public TransactionLogEvent(Object source, TransactionLogRecord record, Map<String, String> details) {
        this(source, record, details, null);
    }

    public TransactionLogEvent(Object source, TransactionLogRecord record, Map<String, String> details, LogPolicyDecision logPolicy) {
        super(source);
        this.record = record;
        this.details = details;
        this.logPolicy = logPolicy;
    }
}
