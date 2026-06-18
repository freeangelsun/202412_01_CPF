package cpf.pfw.common.logging;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@Getter
public class TransactionLogEvent extends ApplicationEvent {

    private final TransactionLogRecord record;
    private final Map<String, String> details;

    public TransactionLogEvent(Object source, TransactionLogRecord record, Map<String, String> details) {
        super(source);
        this.record = record;
        this.details = details;
    }
}

