package com.cpf.core.common.logging.file;

import com.cpf.core.common.logging.TransactionLogEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 온라인 거래 이벤트를 구조화 파일 로그로 연결합니다.
 */
@Component
@RequiredArgsConstructor
public class TransactionFileLogListener {
    private final CpfFileLogWriter fileLogWriter;

    @EventListener
    public void handleTransactionLogEvent(TransactionLogEvent event) {
        fileLogWriter.writeTransaction(event.getRecord(), event.getDetails(), event.getLogPolicy());
    }
}
