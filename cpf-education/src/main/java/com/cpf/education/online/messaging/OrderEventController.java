package com.cpf.education.online.messaging;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.annotation.CpfService;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.messaging.api.CpfBrokerBridgeMessage;
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import com.cpf.messaging.api.CpfMessageListener;
import com.cpf.messaging.api.CpfMessagingTemplate;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인-15 Messaging·비동기: 발행과 Consumer를 CPF Provider-neutral 계약으로 연결합니다. */
@CpfRestController
@RequestMapping("/edu/online/orders")
public class OrderEventController {
    private final CpfMessagingTemplate messaging;

    public OrderEventController(CpfMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @PostMapping
    @Operation(operationId = "EDU_ORDER_EVENT_PUBLISH", summary = "Messaging·비동기 거래")
    @CpfOnlineTransaction(
            operationId = "EDU_ORDER_EVENT_PUBLISH",
            name = "Messaging·비동기 거래",
            description = "CPF Messaging이 Context·correlation·idempotency를 전파하고 Listener Runtime이 retry/DLQ/duplicate 정책을 적용한다.")
    /** send 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public CpfBrokerPublishResult send(@RequestBody Command command) {
        CpfBrokerPublishRequest request = new CpfBrokerPublishRequest(
                command.messageId(),
                "edu.member.changed",
                command.memberId(),
                command.payload().getBytes(StandardCharsets.UTF_8),
                "application/json",
                "EDU",
                "EXS",
                command.idempotencyKey(),
                Map.of(),
                Map.of("correlationId", CpfContexts.transactionId()));
        return messaging.send(request);
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record Command(String messageId, String memberId, String payload, String idempotencyKey) { }

    /** Listener Runtime이 Context/멱등성을 복원한 뒤 호출하는 Consumer입니다. 예외는 Runtime retry/DLQ 정책으로 전달합니다. */
    @CpfService
    public static class MemberChangedConsumer {
        private final AtomicReference<String> lastKey = new AtomicReference<>();

        @CpfMessageListener(destination = "edu.member.changed", consumerGroup = "edu-member-consumer")
        public void consume(CpfBrokerBridgeMessage message) {
            String previous = lastKey.getAndSet(message.key());
            if (message.key().equals(previous)) return; // duplicate delivery: business side effect zero
        }
    }
}
