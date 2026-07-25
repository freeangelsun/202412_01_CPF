package com.cpf.core.api.reliability;

import java.time.Instant;

/** 자동복구 결정/실행 결과를 Audit/Metric/Alert backend로 전달하는 확장 Port입니다. */
@FunctionalInterface
public interface CpfSelfHealingEventSink {
    void publish(Event event);

    record Event(
            String targetKey,
            String actionType,
            String state,
            String reason,
            String message,
            Instant occurredAt) {
        public Event {
            occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        }
    }

    static CpfSelfHealingEventSink noop() { return event -> { }; }
}
