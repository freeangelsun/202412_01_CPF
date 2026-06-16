package cpf.pfw.common.logging;

import cpf.pfw.service.common.logging.TransactionLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * ?몃옖??뀡 濡쒓렇 ?대깽?몃? ?섏떊?섏뿬 ?꾨젅?꾩썙??濡쒓렇 ?뚯씠釉붿뿉 ??ν빀?덈떎.
 */
@Component
@RequiredArgsConstructor
public class TransactionLogListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionLogListener.class);

    private final TransactionLogService logService;

    /**
     * 濡쒓렇 ?곸옱 ?ㅽ뙣媛 ?ㅼ젣 ?낅Т 嫄곕옒 ?ㅽ뙣濡?踰덉?吏 ?딅룄濡??덉쇅瑜??≪닔?섍퀬 寃쎄퀬 濡쒓렇留??④퉩?덈떎.
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

