package com.cpf.testkit.fixture;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** CPF capability 경계의 호출/복구 순서를 기술 독립적으로 기록하는 probe. */
public final class CpfProtocolProbe {
    public enum Channel {
        HTTP, DB, MESSAGING, OUTBOX, INBOX, SAGA, TCC, BATCH,
        OBJECT_STORAGE, HEALTH, GRAPHQL
    }

    public record Event(Channel channel, String operation, String outcome,
                        String transactionId, String instanceId, Instant at,
                        Map<String, String> attributes) {
        public Event {
            Objects.requireNonNull(channel, "channel");
            require(operation, "operation");
            require(outcome, "outcome");
            require(transactionId, "transactionId");
            require(instanceId, "instanceId");
            Objects.requireNonNull(at, "at");
            attributes = Map.copyOf(attributes == null ? Map.of() : attributes);
        }
        private static void require(String value, String name) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private final CopyOnWriteArrayList<Event> events = new CopyOnWriteArrayList<>();
    public void record(Event event) { events.add(Objects.requireNonNull(event, "event")); }
    public List<Event> snapshot() { return List.copyOf(events); }
    public List<Event> byChannel(Channel channel) {
        List<Event> result = new ArrayList<>();
        for (Event event : events) if (event.channel() == channel) result.add(event);
        return List.copyOf(result);
    }
    public void clear() { events.clear(); }
}
