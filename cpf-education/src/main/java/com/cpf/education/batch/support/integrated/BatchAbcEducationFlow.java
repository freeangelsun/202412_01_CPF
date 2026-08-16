package com.cpf.education.batch.support.integrated;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Scheduler/Operator → Job → Step/Chunk → Domain A → B → C → DB/Remote를 연결하는 실행 가능한 Batch 참조 흐름이다.
 * transactionId와 job/execution/step/attempt/fence identity를 분리하고 checkpoint/restart, retry/skip, stale lease,
 * multi-instance competition, process-kill, UNKNOWN/reconcile, duplicate side-effect 방지를 검증할 수 있게 설계한다.
 */
public final class BatchAbcEducationFlow {
    public enum State { RUNNING, RETRYING, SUCCESS, UNKNOWN, FAILED }
    public record Identity(String transactionId, String jobId, String executionId, String stepId, int attempt, long fenceToken) {}
    public record Item(String businessKey, String payload) {}
    public record Event(String transactionId, String jobId, String executionId, String stepId, int attempt,
                        String businessKey, String state, String detail) {}
    public record Snapshot(Identity identity, State state, int checkpoint, int committed, int remoteEffects,
                           int skipped, List<Event> events) {}

    /** Lease 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public static final class Lease {
        private final AtomicLong fence = new AtomicLong();
        private volatile String owner;
        public synchronized long acquire(String candidate) {
            if (owner != null && !owner.equals(candidate)) throw new LeaseBusy();
            owner = candidate;
            return fence.incrementAndGet();
        }
        /** valid 작업을 CPF 표준 계약에 따라 수행한다. */
        public synchronized boolean valid(String candidate, long token) {
            return candidate != null && candidate.equals(owner) && fence.get() == token;
        }
        public synchronized void release(String candidate, long token) {
            if (valid(candidate, token)) owner = null;
        }
        public synchronized void forceExpire() { owner = null; }
        /** stealForTest 작업을 CPF 표준 계약에 따라 수행한다. */
        public synchronized void stealForTest(String candidate) { owner = candidate; fence.incrementAndGet(); }
    }
    public static final class LeaseBusy extends RuntimeException {}

    public static class Store {
        protected final Set<String> committed = ConcurrentHashMap.newKeySet();
        protected final Set<String> skipped = ConcurrentHashMap.newKeySet();
        protected final Map<String, Integer> retryCount = new ConcurrentHashMap<>();
        protected volatile int checkpoint;
        /** checkpoint 작업을 CPF 표준 계약에 따라 수행한다. */
        public synchronized int checkpoint() { return checkpoint; }
        public synchronized boolean terminal(String key) { return committed.contains(key) || skipped.contains(key); }
        public synchronized boolean committed(String key) { return committed.contains(key); }
        public synchronized void markCommitted(String key) { committed.add(key); onMutation(); }
        public synchronized void markSkipped(String key) { skipped.add(key); onMutation(); }
        public synchronized void markCheckpoint(int value) { checkpoint = value; onMutation(); }
        public synchronized void incrementRetry(String key) { retryCount.merge(key, 1, Integer::sum); onMutation(); }
        /** committedCount 작업을 CPF 표준 계약에 따라 수행한다. */
        public synchronized int committedCount() { return committed.size(); }
        public synchronized int skippedCount() { return skipped.size(); }
        public Set<String> committedKeys() { return Set.copyOf(committed); }
        public Set<String> skippedKeys() { return Set.copyOf(skipped); }
        protected void onMutation() {}
    }

    /** JVM 종료/Process Kill 뒤에도 checkpoint와 terminal item을 보존하는 파일 기반 참조 저장소다. */
    public static final class FileStore extends Store {
        private final Path path;
        public FileStore(Path path) {
            this.path = path.toAbsolutePath().normalize();
            load();
        }
        @Override protected synchronized void onMutation() { persist(); }
        private void load() {
            if (!Files.exists(path)) return;
            Properties p = new Properties();
            // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
            try (var in = Files.newInputStream(path)) { p.load(in); } catch (IOException e) { throw new IllegalStateException("batch-store-load", e); }
            checkpoint = Integer.parseInt(p.getProperty("checkpoint", "0"));
            parseSet(p.getProperty("committed", ""), committed);
            parseSet(p.getProperty("skipped", ""), skipped);
            for (String name : p.stringPropertyNames()) if (name.startsWith("retry.")) retryCount.put(name.substring(6), Integer.parseInt(p.getProperty(name)));
        }
        private void persist() {
            Properties p = new Properties();
            p.setProperty("checkpoint", Integer.toString(checkpoint));
            p.setProperty("committed", String.join(",", committed));
            p.setProperty("skipped", String.join(",", skipped));
            retryCount.forEach((k,v) -> p.setProperty("retry." + k, Integer.toString(v)));
            try {
                Files.createDirectories(path.getParent());
                Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
                try (var out = Files.newOutputStream(tmp)) { p.store(out, "CPF batch education durable state"); }
                try { Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
                // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
                catch (IOException unsupported) { Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING); }
            } catch (IOException e) { throw new IllegalStateException("batch-store-persist", e); }
        }
        private static void parseSet(String raw, Set<String> out) {
            if (raw == null || raw.isBlank()) return;
            for (String v : raw.split(",")) if (!v.isBlank()) out.add(v);
        }
    }

    /** Remote 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public static class Remote {
        protected final Set<String> effects = ConcurrentHashMap.newKeySet();
        protected final Map<String, Integer> calls = new ConcurrentHashMap<>();
        public volatile String timeoutKey;
        public volatile String failKey;
        public volatile String failOnceKey;
        public synchronized void send(String key) {
            int n = calls.merge(key, 1, Integer::sum);
            if (key.equals(timeoutKey)) throw new TimeoutExceptionUnchecked();
            if (key.equals(failKey)) throw new IllegalStateException("remote-failure:" + key);
            if (key.equals(failOnceKey) && n == 1) throw new TimeoutExceptionUnchecked();
            effects.add(key);
        }
        /** compensate 작업을 CPF 표준 계약에 따라 수행한다. */
        public synchronized void compensate(String key) { effects.remove(key); }
        public Set<String> effectKeys() { return Set.copyOf(effects); }
        public int calls(String key) { return calls.getOrDefault(key, 0); }
    }

    /** Process Kill/Restart 사이에도 원격 side-effect 멱등성 증거를 보존하는 파일 기반 Remote다. */
    public static final class FileRemote extends Remote {
        private final Path path;
        public FileRemote(Path path) { this.path = path.toAbsolutePath().normalize(); load(); }
        @Override public synchronized void send(String key) { super.send(key); persist(); }
        @Override public synchronized void compensate(String key) { super.compensate(key); persist(); }
        private void load() {
            if (!Files.exists(path)) return;
            Properties p = new Properties();
            try (var in = Files.newInputStream(path)) { p.load(in); } catch (IOException e) { throw new IllegalStateException("batch-remote-load", e); }
            parseSet(p.getProperty("effects", ""), effects);
            for (String name : p.stringPropertyNames()) if (name.startsWith("calls.")) calls.put(name.substring(6), Integer.parseInt(p.getProperty(name)));
        }
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        private synchronized void persist() {
            Properties p = new Properties();
            p.setProperty("effects", String.join(",", effects));
            calls.forEach((k,v) -> p.setProperty("calls." + k, Integer.toString(v)));
            try {
                Files.createDirectories(path.getParent());
                Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
                try (var out = Files.newOutputStream(tmp)) { p.store(out, "CPF batch education remote effects"); }
                try { Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
                catch (IOException unsupported) { Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING); }
            // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
            } catch (IOException e) { throw new IllegalStateException("batch-remote-persist", e); }
        }
        private static void parseSet(String raw, Set<String> out) {
            if (raw == null || raw.isBlank()) return;
            for (String v : raw.split(",")) if (!v.isBlank()) out.add(v);
        }
    }
    public static final class TimeoutExceptionUnchecked extends RuntimeException {}
    public static final class ProcessKilled extends RuntimeException {}

    /** SchedulerOperator 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public static final class SchedulerOperator {
        private final Job job;
        public SchedulerOperator(Job job) { this.job = job; }
        public Snapshot launch(Identity id, List<Item> items, String owner) { return job.run(id, items, owner); }
    }

    public static final class Job {
        private final Step step;
        private final Lease lease;
        /** Job 작업을 CPF 표준 계약에 따라 수행한다. */
        public Job(Step step, Lease lease) { this.step = step; this.lease = lease; }
        public Snapshot run(Identity id, List<Item> items, String owner) {
            final long fence;
            try { fence = lease.acquire(owner); }
            catch (LeaseBusy busy) { return step.busy(id, items, owner); }
            Identity fenced = new Identity(id.transactionId(), id.jobId(), id.executionId(), id.stepId(), id.attempt(), fence);
            try { return step.chunk(fenced, items, owner, lease); }
            finally { lease.release(owner, fence); }
        }
    }

    /** Step 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public static final class Step {
        private final Store store;
        private final DomainA a;
        private final int chunkSize;
        private final int maxRetries;
        public volatile int killAfter = -1;
        public volatile int stealLeaseAfter = -1;
        public volatile Runnable afterCheckpointHook;
        /** Step 작업을 CPF 표준 계약에 따라 수행한다. */
        public Step(Store store, DomainA a, int chunkSize, int maxRetries) {
            this.store = store; this.a = a; this.chunkSize = chunkSize; this.maxRetries = maxRetries;
        }
        Snapshot busy(Identity id, List<Item> items, String owner) {
            return snapshot(id, State.RETRYING, List.of(event(id, null, "LEASE_BUSY", owner)));
        }
        public Snapshot chunk(Identity id, List<Item> items, String owner, Lease lease) {
            List<Event> events = new ArrayList<>();
            int i = store.checkpoint();
            try {
                for (; i < items.size(); i++) {
                    Item item = items.get(i);
                    if (store.terminal(item.businessKey())) {
                        events.add(event(id, item.businessKey(), "DUPLICATE_SKIP", "already-terminal"));
                        advanceCheckpoint(i, items.size());
                        continue;
                    }
                    if (stealLeaseAfter == i) lease.stealForTest("OTHER");
                    if (!lease.valid(owner, id.fenceToken())) {
                        events.add(event(id, item.businessKey(), "STALE_FENCE", "lease-lost"));
                        return snapshot(id, State.UNKNOWN, events);
                    }
                    int tryNo = 0;
                    while (true) {
                        tryNo++;
                        try {
                            a.handle(id, item, events);
                            break;
                        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
                        } catch (TimeoutExceptionUnchecked timeout) {
                            store.incrementRetry(item.businessKey());
                            events.add(event(id, item.businessKey(), "RETRY", "try=" + tryNo));
                            if (tryNo > maxRetries) return snapshot(id, State.RETRYING, events);
                        } catch (SkippableItemException skippable) {
                            store.markSkipped(item.businessKey());
                            events.add(event(id, item.businessKey(), "SKIPPED", skippable.getMessage()));
                            break;
                        }
                    }
                    advanceCheckpoint(i, items.size());
                    if (killAfter == i) throw new ProcessKilled();
                }
                return snapshot(id, State.SUCCESS, events);
            // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
            } catch (ProcessKilled killed) {
                events.add(event(id, i < items.size() ? items.get(i).businessKey() : null, "PROCESS_KILLED", "restart-required"));
                return snapshot(id, State.UNKNOWN, events);
            } catch (LocalDbFailure dbFailure) {
                String key = i < items.size() ? items.get(i).businessKey() : null;
                events.add(event(id, key, "ROLLBACK", "local-db-failure"));
                events.add(event(id, key, "UNKNOWN", "remote-side-effect-reconcile-required"));
                return snapshot(id, State.UNKNOWN, events);
            } catch (RuntimeException ex) {
                String key = i < items.size() ? items.get(i).businessKey() : null;
                // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
                events.add(event(id, key, "ROLLBACK", "before-commit"));
                events.add(event(id, key, "FAILED", ex.getClass().getSimpleName()));
                return snapshot(id, State.FAILED, events);
            }
        }
        private void advanceCheckpoint(int i, int size) {
            if ((i + 1) % chunkSize == 0 || i + 1 == size) store.markCheckpoint(i + 1);
            if (afterCheckpointHook != null) afterCheckpointHook.run();
        }
        private Snapshot snapshot(Identity id, State state, List<Event> events) {
            return new Snapshot(id, state, store.checkpoint(), store.committedCount(), a.c.remote.effectKeys().size(),
                    store.skippedCount(), List.copyOf(events));
        }
        private static Event event(Identity id, String key, String state, String detail) {
            return new Event(id.transactionId(), id.jobId(), id.executionId(), id.stepId(), id.attempt(), key, state, detail);
        }
    }

    /** DomainA 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public static final class DomainA {
        private final DomainB b;
        private final DomainC c;
        public DomainA(DomainB b, DomainC c) { this.b = b; this.c = c; }
        void handle(Identity id, Item item, List<Event> events) {
            if (b.store.committed(item.businessKey())) return;
            events.add(Step.event(id, item.businessKey(), "DOMAIN_A", "begin"));
            c.call(id, item, events);
            b.commit(id, item, events);
        }
    }

    /** DomainB 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public static final class DomainB {
        private final Store store;
        public volatile String dbFailKey;
        public volatile String skippableKey;
        public DomainB(Store store) { this.store = store; }
        void commit(Identity id, Item item, List<Event> events) {
            if (item.businessKey().equals(skippableKey)) throw new SkippableItemException("validation:" + item.businessKey());
            if (item.businessKey().equals(dbFailKey)) throw new LocalDbFailure(item.businessKey());
            store.markCommitted(item.businessKey());
            events.add(Step.event(id, item.businessKey(), "COMMIT", "db"));
        }
    }

    /** DomainC 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public static final class DomainC {
        private final Remote remote;
        public DomainC(Remote remote) { this.remote = remote; }
        void call(Identity id, Item item, List<Event> events) {
            events.add(Step.event(id, item.businessKey(), "REMOTE_BEGIN", "call"));
            remote.send(item.businessKey());
            events.add(Step.event(id, item.businessKey(), "REMOTE_SUCCESS", "call"));
        }
    }

    /** SkippableItemException 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public static final class SkippableItemException extends RuntimeException {
        public SkippableItemException(String message) { super(message); }
    }
    public static final class LocalDbFailure extends RuntimeException {
        public LocalDbFailure(String key) { super(key); }
    }

    /** UNKNOWN에서 원격 side effect만 남은 키를 명시적으로 보상한다. */
    public static final class Reconciler {
        private final Store store;
        private final Remote remote;
        public Reconciler(Store store, Remote remote) { this.store = store; this.remote = remote; }
        public void reconcile(String businessKey) {
            if (!store.committed(businessKey)) remote.compensate(businessKey);
        }
    }

    private BatchAbcEducationFlow() {}
}
