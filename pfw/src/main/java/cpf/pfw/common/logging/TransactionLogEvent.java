package cpf.pfw.common.logging;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * ?몃옖??뀡 濡쒓렇 ?대깽???대옒??
 * 濡쒓렇 ?곗씠?곕? DB????ν븯湲??꾪븳 ?대깽???뺤쓽?낅땲??
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

