package cpf.pfw.common.logging;

import cpf.pfw.service.common.logging.TransactionLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CPF 기능 설명입니다.
 */
@Component
@RequiredArgsConstructor
public class TransactionLogListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionLogListener.class);

    private final TransactionLogService logService;

    /**
     * CPF 기능 설명입니다.
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

