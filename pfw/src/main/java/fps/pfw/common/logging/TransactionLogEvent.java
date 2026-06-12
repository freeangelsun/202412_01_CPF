package fps.pfw.common.logging;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * 트랜잭션 로그 이벤트 클래스.
 * 로그 데이터를 DB에 저장하기 위한 이벤트 정의입니다.
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
