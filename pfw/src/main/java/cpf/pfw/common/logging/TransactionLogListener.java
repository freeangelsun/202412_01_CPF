package cpf.pfw.common.logging;

import cpf.pfw.service.common.logging.TransactionLogService;
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

    /**
     * 거래 로그 저장 실패가 업무 응답 실패로 번지지 않도록 경고 로그만 남깁니다.
     */
    @EventListener
    public void handleTransactionLogEvent(TransactionLogEvent event) {
        try {
            logService.saveTransactionLog(event.getRecord(), event.getDetails(), event.getLogPolicy());
        } catch (Exception e) {
            String transactionId = event.getRecord() != null ? event.getRecord().getTransactionId() : "N/A";
            log.warn("Failed to persist transaction log. transactionId={}", transactionId, e);
        }
    }
}
