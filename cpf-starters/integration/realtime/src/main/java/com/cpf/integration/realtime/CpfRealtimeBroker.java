package com.cpf.integration.realtime;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * SSE/Long-poll 공통 Runtime입니다.
 * replay, stable event-id duplicate fence, bounded backpressure, tenant connection/rate limit,
 * multi-instance fan-out, graceful drain을 한 곳에서 보장합니다.
 */
public final class CpfRealtimeBroker implements AutoCloseable {
    private final String instanceId;
    private final CpfRealtimeProperties properties;
    private final CpfRealtimeBackplane backplane;
    private final Clock clock;
    private final AtomicLong sequence = new AtomicLong();
    private final ArrayDeque<CpfRealtimeEvent> replay = new ArrayDeque<>();
    private final Set<String> seenEventIds = new HashSet<>();
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> tenantConnections = new ConcurrentHashMap<>();
    private final Map<String, RateWindow> rateWindows = new HashMap<>();
    private final Object replayLock = new Object();
    private final Object rateLock = new Object();
    private final ExecutorService deliveryExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "cpf-realtime-heartbeat");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final AutoCloseable backplaneSubscription;

    public CpfRealtimeBroker(String instanceId, CpfRealtimeProperties properties, CpfRealtimeBackplane backplane) {
        this(instanceId, properties, backplane, Clock.systemUTC());
    }

    CpfRealtimeBroker(String instanceId, CpfRealtimeProperties properties, CpfRealtimeBackplane backplane, Clock clock) {
        this.instanceId = require(instanceId, "instanceId");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.backplane = Objects.requireNonNull(backplane, "backplane");
        this.clock = Objects.requireNonNull(clock, "clock");
        try {
            this.backplaneSubscription = backplane.subscribe(this::acceptRemote);
        } catch (RuntimeException e) {
            throw e;
        }
        scheduler.scheduleAtFixedRate(this::heartbeatSafe,
                properties.getHeartbeatInterval().toMillis(),
                properties.getHeartbeatInterval().toMillis(), TimeUnit.MILLISECONDS);
    }

    /** Local producer publish. 동일 eventId 재전송은 한 번만 fan-out합니다. */
    public CpfRealtimeEvent publish(CpfRealtimeEvent input) {
        ensureAccepting();
        CpfRealtimeEvent stored = storeIfNew(input);
        if (stored == null) return findByEventId(input.eventId());
        fanOut(stored);
        backplane.publish(instanceId, stored);
        return stored;
    }

    public Subscription subscribe(Filter filter, String afterEventId, Consumer<Delivery> consumer) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(consumer, "consumer");
        ensureAccepting();
        enforceRate(filter.tenantId());
        AtomicInteger count = tenantConnections.computeIfAbsent(filter.tenantId(), ignored -> new AtomicInteger());
        if (count.incrementAndGet() > properties.getMaxConnectionsPerTenant()) {
            count.decrementAndGet();
            throw new RealtimeLimitException("tenant connection limit exceeded");
        }
        String id = UUID.randomUUID().toString();
        Subscription subscription = new Subscription(id, filter, consumer, properties.getSubscriberQueueCapacity(), count);
        subscriptions.put(id, subscription);
        for (CpfRealtimeEvent event : replayAfterEventId(afterEventId, filter, properties.getReplayCapacity())) {
            if (!subscription.offer(new Delivery.Event(event))) {
                subscription.close("replay-backpressure");
                throw new RealtimeLimitException("subscriber too slow during replay");
            }
        }
        subscription.start();
        return subscription;
    }

    public List<CpfRealtimeEvent> poll(Filter filter, String afterEventId, int requestedLimit) {
        Objects.requireNonNull(filter, "filter");
        ensureAccepting();
        enforceRate(filter.tenantId());
        int limit = Math.min(Math.max(1, requestedLimit), properties.getPollLimit());
        return replayAfterEventId(afterEventId, filter, limit);
    }

    public boolean isAccepting() { return accepting.get(); }
    public int activeSubscriptions() { return subscriptions.size(); }

    /** 신규 ingress를 먼저 닫고 기존 subscriber queue를 drain한 뒤 종료합니다. */
    public void drain() {
        if (!accepting.compareAndSet(true, false)) return;
        long deadline = System.nanoTime() + properties.getDrainTimeout().toNanos();
        while (System.nanoTime() < deadline) {
            boolean pending = subscriptions.values().stream().anyMatch(Subscription::hasPending);
            if (!pending) break;
            try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
        for (Subscription subscription : List.copyOf(subscriptions.values())) subscription.close("server-drain");
    }

    @Override public void close() {
        drain();
        scheduler.shutdownNow();
        deliveryExecutor.shutdown();
        try { backplaneSubscription.close(); } catch (Exception ignored) { }
    }

    private void acceptRemote(CpfRealtimeBackplane.RemoteEvent remote) {
        if (!accepting.get() || instanceId.equals(remote.originInstanceId())) return;
        CpfRealtimeEvent stored = storeIfNew(remote.event());
        if (stored != null) fanOut(stored);
    }

    private CpfRealtimeEvent storeIfNew(CpfRealtimeEvent input) {
        synchronized (replayLock) {
            if (seenEventIds.contains(input.eventId())) return null;
            long next = sequence.incrementAndGet();
            CpfRealtimeEvent stored = input.withSequence(next);
            replay.addLast(stored);
            seenEventIds.add(stored.eventId());
            while (replay.size() > properties.getReplayCapacity()) {
                CpfRealtimeEvent removed = replay.removeFirst();
                seenEventIds.remove(removed.eventId());
            }
            return stored;
        }
    }

    private CpfRealtimeEvent findByEventId(String eventId) {
        synchronized (replayLock) {
            for (CpfRealtimeEvent event : replay) if (event.eventId().equals(eventId)) return event;
            return null;
        }
    }

    private List<CpfRealtimeEvent> replayAfterEventId(String afterEventId, Filter filter, int limit) {
        List<CpfRealtimeEvent> out = new ArrayList<>();
        String cursor = afterEventId == null ? "" : afterEventId.trim();
        synchronized (replayLock) {
            boolean emit = cursor.isEmpty();
            boolean cursorFound = cursor.isEmpty();
            for (CpfRealtimeEvent event : replay) {
                if (!emit && event.eventId().equals(cursor)) {
                    emit = true; cursorFound = true; continue;
                }
                if (!emit || !filter.matches(event)) continue;
                out.add(event);
                if (out.size() >= limit) break;
            }
            // If the cursor aged out of the bounded replay window, fail closed instead of silently skipping a gap.
            if (!cursorFound) throw new RealtimeReplayGapException("realtime replay cursor is no longer available");
        }
        return List.copyOf(out);
    }

    private void fanOut(CpfRealtimeEvent event) {
        for (Subscription subscription : subscriptions.values()) {
            if (!subscription.filter.matches(event)) continue;
            if (!subscription.offer(new Delivery.Event(event))) subscription.close("slow-consumer-backpressure");
        }
    }

    private void heartbeatSafe() {
        try {
            Instant now = clock.instant();
            for (Subscription subscription : subscriptions.values()) {
                if (!subscription.offer(new Delivery.Heartbeat(now))) subscription.close("slow-consumer-heartbeat");
            }
        } catch (RuntimeException ignored) {
            // heartbeat failure must not stop scheduler; individual subscriber is fenced by bounded queue.
        }
    }

    private void enforceRate(String tenantId) {
        synchronized (rateLock) {
            long second = clock.instant().getEpochSecond();
            RateWindow window = rateWindows.get(tenantId);
            if (window == null || window.epochSecond != second) {
                rateWindows.put(tenantId, new RateWindow(second, 1));
                return;
            }
            if (++window.count > properties.getMaxSubscribeAttemptsPerSecond())
                throw new RealtimeLimitException("tenant realtime rate limit exceeded");
        }
    }

    private void ensureAccepting() {
        if (!accepting.get()) throw new RealtimeUnavailableException("realtime runtime is draining");
    }

    private static String require(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name);
        return value.trim();
    }

    private static final class RateWindow {
        private final long epochSecond;
        private int count;
        private RateWindow(long epochSecond, int count) { this.epochSecond = epochSecond; this.count = count; }
    }

    public final class Subscription implements AutoCloseable {
        private final String id;
        private final Filter filter;
        private final Consumer<Delivery> consumer;
        private final ArrayBlockingQueue<Delivery> queue;
        private final AtomicInteger tenantCount;
        private final AtomicBoolean closed = new AtomicBoolean();

        private Subscription(String id, Filter filter, Consumer<Delivery> consumer, int capacity, AtomicInteger tenantCount) {
            this.id = id; this.filter = filter; this.consumer = consumer;
            this.queue = new ArrayBlockingQueue<>(capacity); this.tenantCount = tenantCount;
        }
        private void start() {
            deliveryExecutor.submit(() -> {
                while (!closed.get()) {
                    try {
                        Delivery delivery = queue.poll(250, TimeUnit.MILLISECONDS);
                        if (delivery != null) consumer.accept(delivery);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); close("interrupted");
                    } catch (RuntimeException e) {
                        close("consumer-error");
                    }
                }
            });
        }
        private boolean offer(Delivery delivery) { return !closed.get() && queue.offer(delivery); }
        private boolean hasPending() { return !queue.isEmpty(); }
        public String id() { return id; }
        public String closeReason() { return closeReason; }
        private volatile String closeReason = "";
        private void close(String reason) {
            if (!closed.compareAndSet(false, true)) return;
            closeReason = reason;
            subscriptions.remove(id, this);
            tenantCount.decrementAndGet();
        }
        @Override public void close() { close("client-close"); }
    }

    public sealed interface Delivery permits Delivery.Event, Delivery.Heartbeat {
        record Event(CpfRealtimeEvent event) implements Delivery { }
        record Heartbeat(Instant at) implements Delivery { }
    }

    public record Filter(String tenantId, String channel, String topic, String subjectId) {
        public Filter {
            tenantId = require(tenantId, "tenantId");
            channel = require(channel, "channel");
            topic = require(topic, "topic");
            subjectId = subjectId == null ? "" : subjectId.trim();
        }
        boolean matches(CpfRealtimeEvent event) {
            return tenantId.equals(event.tenantId()) && channel.equals(event.channel()) && topic.equals(event.topic())
                    && (subjectId.isEmpty() || subjectId.equals(event.subjectId()));
        }
    }

    public static class RealtimeLimitException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public RealtimeLimitException(String message) { super(message); }
    }
    public static class RealtimeUnavailableException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public RealtimeUnavailableException(String message) { super(message); }
    }
    public static class RealtimeReplayGapException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public RealtimeReplayGapException(String message) { super(message); }
    }
}
