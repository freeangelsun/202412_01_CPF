package member.sample.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** 민감정보 원문을 남기지 않고 TransactionId 중심의 구조화 업무 Audit event를 기록합니다. */
@Component
public class DomainAuditLogger {
    private static final Logger log = LoggerFactory.getLogger(DomainAuditLogger.class);
    public void success(String action, String transactionId, String entityId) {
        log.info("cpfAudit domain=member system=MBR action={} transactionId={} entityId={} result=SUCCESS", action, transactionId, entityId);
    }
    public void replay(String transactionId, String entityId) {
        log.info("cpfAudit domain=member system=MBR action=IDEMPOTENT_REPLAY transactionId={} entityId={} result=SUCCESS", transactionId, entityId);
    }
}
