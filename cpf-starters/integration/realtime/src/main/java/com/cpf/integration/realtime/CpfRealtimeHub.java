package com.cpf.integration.realtime;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Realtime connection과 message fan-out을 관리하는 CPF Public Runtime 계약입니다.
 * <p>연결 생명주기와 전송 상태를 한 곳에서 관리하며 Controller나 업무 Service가 세션 저장소를 직접 조작하지 않게 합니다.
 */
public final class CpfRealtimeHub implements AutoCloseable {
    private record Client(String tenant, String user, SseEmitter emitter) {}
    private record Event(long sequence, String id, String tenant, Object data) {}
    private static final class TopicState {
        final ConcurrentMap<Long, Client> clients = new ConcurrentHashMap<>();
        final Deque<Event> history = new ArrayDeque<>();
    }

    private final CpfRealtimeProperties properties;
    private final ConcurrentMap<String, TopicState> topics = new ConcurrentHashMap<>();
    private final AtomicLong clientIds = new AtomicLong();
    private final AtomicLong eventIds = new AtomicLong();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("cpf-realtime-heartbeat-", 0).factory());

    public CpfRealtimeHub(CpfRealtimeProperties properties) {
        this.properties = Objects.requireNonNull(properties);
        long millis = properties.getHeartbeatInterval().toMillis();
        scheduler.scheduleAtFixedRate(this::heartbeat, millis, millis, TimeUnit.MILLISECONDS);
    }

    public SseEmitter subscribe(String topic, String tenant, String user, String lastEventId) {
        String normalizedTopic = safe(topic);
        if (user == null || user.isBlank()) throw new SecurityException("authenticated user required");
        if (tenant == null || tenant.isBlank()) throw new SecurityException("tenant required");
        TopicState state = topics.computeIfAbsent(normalizedTopic, key -> new TopicState());
        if (tenantClientCount(tenant) >= properties.getMaxConnectionsPerTenant()) {
            throw new IllegalStateException("realtime capacity exceeded");
        }
        long clientId = clientIds.incrementAndGet();
        var emitter = new SseEmitter(properties.getEmitterTimeout().toMillis());
        state.clients.put(clientId, new Client(tenant, user, emitter));
        Runnable remove = () -> state.clients.remove(clientId);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(error -> remove.run());
        replay(state, tenant, lastEventId, emitter);
        return emitter;
    }

    public long publish(String topic, String tenant, String eventId, Object data) {
        TopicState state = topics.computeIfAbsent(safe(topic), key -> new TopicState());
        long sequence = eventIds.incrementAndGet();
        String canonicalEventId = eventId == null || eventId.isBlank() ? Long.toString(sequence) : eventId;
        Event event = new Event(sequence, canonicalEventId, tenant, data);
        synchronized (state.history) {
            state.history.addLast(event);
            while (state.history.size() > properties.getReplayCapacity()) state.history.removeFirst();
        }
        long sent = 0;
        for (Map.Entry<Long, Client> entry : state.clients.entrySet()) {
            Client client = entry.getValue();
            if (tenant != null && !Objects.equals(tenant, client.tenant())) continue;
            if (send(client.emitter(), event)) sent++; else state.clients.remove(entry.getKey());
        }
        return sent;
    }

    private void replay(TopicState state, String tenant, String lastEventId, SseEmitter emitter) {
        if (lastEventId == null || lastEventId.isBlank()) return;
        List<Event> snapshot;
        synchronized (state.history) { snapshot = new ArrayList<>(state.history); }
        boolean found = false;
        for (Event event : snapshot) {
            if (!found) {
                found = event.id().equals(lastEventId);
                continue;
            }
            if (event.tenant() != null && !Objects.equals(event.tenant(), tenant)) continue;
            if (!send(emitter, event)) return;
        }
        if (!found && !snapshot.isEmpty()) {
            throw new IllegalStateException("Last-Event-ID is outside replay window");
        }
    }

    private static boolean send(SseEmitter emitter, Event event) {
        try {
            emitter.send(SseEmitter.event().id(event.id()).name("message").data(event.data()));
            return true;
        } catch (IOException | IllegalStateException failure) {
            emitter.completeWithError(failure);
            return false;
        }
    }

    private void heartbeat() {
        topics.forEach((topic, state) -> state.clients.forEach((id, client) -> {
            try { client.emitter().send(SseEmitter.event().name("heartbeat").data("ok")); }
            catch (IOException | IllegalStateException error) { state.clients.remove(id); }
        }));
    }

    private int tenantClientCount(String tenant) {
        return topics.values().stream()
                .flatMap(state -> state.clients.values().stream())
                .mapToInt(client -> Objects.equals(tenant, client.tenant()) ? 1 : 0)
                .sum();
    }
    private static String safe(String topic) { if (topic == null || !topic.matches("[A-Za-z0-9._-]{1,80}")) throw new IllegalArgumentException("invalid topic"); return topic; }

    @Override public void close() {
        scheduler.shutdownNow();
        topics.values().forEach(state -> state.clients.values().forEach(client -> client.emitter().complete()));
        topics.clear();
    }
}
