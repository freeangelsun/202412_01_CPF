package com.cpf.batch.runtime;
import com.cpf.core.common.batch.CpfBatchEventPublisher;
import com.cpf.core.common.batch.CpfBatchEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * broker가 없는 로컬/개발 환경에서 사용하는 배치 이벤트 fallback 발행기입니다.
 */
public class BatBatchLoggingEventPublisher implements CpfBatchEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(BatBatchLoggingEventPublisher.class);

    @Override
    public void publish(CpfBatchEvent event) {
        log.info("BAT 배치 이벤트 fallback 발행. type={}, jobId={}, executionId={}, transactionId={}, message={}",
                event.eventType(),
                event.jobId(),
                event.cpfExecutionId(),
                event.transactionId(),
                event.message());
    }
}
