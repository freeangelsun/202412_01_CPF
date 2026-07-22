package com.cpf.core.common.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * broker가 없는 로컬/개발 환경에서 사용하는 배치 이벤트 fallback 발행기입니다.
 */
public class CpfBatchLoggingEventPublisher implements CpfBatchEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(CpfBatchLoggingEventPublisher.class);

    @Override
    public void publish(CpfBatchEvent event) {
        log.info("CPF 배치 이벤트 fallback 발행. type={}, jobId={}, executionId={}, transactionGlobalId={}, message={}",
                event.eventType(),
                event.jobId(),
                event.cpfExecutionId(),
                event.transactionGlobalId(),
                event.message());
    }
}
