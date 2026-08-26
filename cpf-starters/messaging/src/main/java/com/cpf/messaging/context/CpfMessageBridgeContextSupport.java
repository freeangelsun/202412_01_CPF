package com.cpf.messaging.context;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Kafka/JMS/RabbitMQ/IBM MQ가 공유하는 provider-neutral Context bridge입니다. */
public final class CpfMessageBridgeContextSupport {
    private final CpfMessageContextAdapter adapter;
    private final Clock clock;

    public CpfMessageBridgeContextSupport(CpfExecutionIdGenerator executionIds) {
        this(executionIds, Clock.systemUTC());
    }

    public CpfMessageBridgeContextSupport(CpfExecutionIdGenerator executionIds, Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.adapter = new CpfMessageContextAdapter(Objects.requireNonNull(executionIds, "executionIds"), clock);
    }

    public Outbound prepareOutbound(String transport, String destination, String messageId, Map<String, String> userHeaders) {
        CpfContext current = CpfContexts.requireCurrent();
        messageId = hasText(messageId) ? messageId.trim() : "MSG-" + UUID.randomUUID();
        String idem = current.operation() == null ? null : current.operation().idempotencyKey();
        CpfMessageContext message = new CpfMessageContext(
                required(transport, "transport"), messageId, null, null, required(destination, "destination"),
                null, null, null, null, messageId, current.execution().attempt(), false,
                null, null, clock.instant(), null, null, null);
        Map<String, String> headers = CpfMessageContextAdapter.mergeUserHeaders(adapter.inject(current, message, idem), userHeaders);
        return new Outbound(messageId, headers, message);
    }

    public CpfMessageContextBundle extractInbound(
            String transport, String messageId, String destination, String producer, String consumerGroup,
            String partition, String offset, int deliveryAttempt, boolean redelivery, String schemaId,
            String schemaVersion, Map<String, String> headers, String standardExecutionId) {
        CpfMessageContext message = new CpfMessageContext(
                required(transport, "transport"), required(messageId, "messageId"), null, null,
                required(destination, "destination"), producer, consumerGroup, partition, offset,
                messageId, Math.max(1, deliveryAttempt), redelivery, schemaId, schemaVersion,
                null, clock.instant(), null, null);
        return adapter.extract(headers == null ? Map.of() : headers, message, standardExecutionId, null);
    }

    public void consume(CpfMessageContextBundle bundle, Runnable handler) {
        adapter.consume(Objects.requireNonNull(bundle, "bundle"), Objects.requireNonNull(handler, "handler"));
    }

    private static String required(String value, String name) {
        if (!hasText(value)) throw new IllegalArgumentException(name);
        return value.trim();
    }
    private static boolean hasText(String value) { return value != null && !value.isBlank(); }

    public record Outbound(String messageId, Map<String, String> headers, CpfMessageContext context) { }
}
