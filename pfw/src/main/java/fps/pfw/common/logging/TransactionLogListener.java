package fps.pfw.common.logging;

import fps.pfw.service.common.logging.TransactionLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 트랜잭션 로그 이벤트를 수신하여 프레임워크 로그 테이블에 저장합니다.
 */
@Component
@RequiredArgsConstructor
public class TransactionLogListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionLogListener.class);

    private final TransactionLogService logService;

    /**
     * 로그 적재 실패가 실제 업무 거래 실패로 번지지 않도록 예외를 흡수하고 경고 로그만 남깁니다.
     */
    @EventListener
    public void handleTransactionLogEvent(TransactionLogEvent event) {
        try {
            logService.saveTransactionLog(event.getRecord(), event.getDetails());
        } catch (Exception e) {
            String transactionId = event.getRecord() != null ? event.getRecord().getTransactionId() : "N/A";
            log.warn("Failed to persist transaction log. transactionId={}", transactionId, e);
        }
    }
}
