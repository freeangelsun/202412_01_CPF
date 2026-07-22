package cpf.pfw.common.logging;

import cpf.pfw.service.common.logging.TransactionLogService;
import cpf.pfw.common.logging.fallback.TransactionLogFallbackStore;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 거래 로그 이벤트를 수신해 PFW 거래 로그 테이블에 저장합니다.
 */
@Component
@RequiredArgsConstructor
public class TransactionLogListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionLogListener.class);

    private final TransactionLogService logService;
    private final TransactionLogFallbackStore fallbackStore;

    /**
     * 거래 로그 저장 실패가 업무 응답 실패로 번지지 않도록 경고 로그만 남깁니다.
     */
    @EventListener
    public void handleTransactionLogEvent(TransactionLogEvent event) {
        try {
            logService.saveTransactionLog(event.getRecord(), event.getDetails(), event.getLogPolicy());
        } catch (Exception e) {
            String transactionId = event.getRecord() != null ? event.getRecord().getTransactionId() : "N/A";
            try {
                boolean created = fallbackStore.enqueue(
                        event.getRecord(),
                        event.getDetails(),
                        event.getLogPolicy(),
                        e);
                log.warn("DB 거래 로그 저장 실패를 durable journal에 보존했습니다. transactionGlobalId={}, created={}",
                        transactionId, created, e);
            } catch (RuntimeException fallbackFailure) {
                log.error("DB 거래 로그와 durable journal 저장이 모두 실패했습니다. transactionGlobalId={}",
                        transactionId, fallbackFailure);
            }
        }
    }
}
