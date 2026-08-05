package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeActualState;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Runtime delivery side effect와 ACK 사이의 crash window를 식별하는 durable Instance Inbox입니다.
 *
 * <p>적용 전 PREPARED를 fsync하고 성공 후 APPLIED를 다시 fsync합니다. Atomic rename을 지원하지
 * 않는 파일시스템에서는 결과불명 위험을 숨기지 않고 fail-closed합니다.</p>
 */
public final class CpfRuntimeInstanceInboxStore {
    public enum State { PREPARED, APPLIED }

    public record Entry(String deliveryId, String changeId, String changeType, long desiredVersion,
                        String payloadHash, State state, String actualHash, Instant updatedAt) {
        public Entry {
            deliveryId = requireJournalText(deliveryId, "deliveryId", 80);
            changeId = requireJournalText(changeId, "changeId", 80);
            changeType = requireJournalText(changeType, "changeType", 80)
                    .toUpperCase(java.util.Locale.ROOT);
            if (desiredVersion < 0L) {
                throw new IllegalArgumentException("Runtime Inbox desiredVersion은 0 이상이어야 합니다.");
            }
            payloadHash = requireJournalText(payloadHash, "payloadHash", 64);
            state = Objects.requireNonNull(state, "Runtime Inbox state");
            actualHash = actualHash == null || actualHash.isBlank() ? null : actualHash.trim();
            if (state == State.PREPARED && actualHash != null) {
                throw new IllegalArgumentException("PREPARED Runtime Inbox에는 actualHash가 없어야 합니다.");
            }
            if (state == State.APPLIED) {
                actualHash = requireJournalText(actualHash, "actualHash", 64);
            }
            updatedAt = Objects.requireNonNull(updatedAt, "Runtime Inbox updatedAt");
        }

        CpfRuntimeActualState toActualState() {
            return new CpfRuntimeActualState(changeType, desiredVersion, actualHash, deliveryId);
        }

        private static String requireJournalText(String value, String field, int maxLength) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Runtime Inbox " + field + "가 필요합니다.");
            }
            String normalized = value.trim();
            if (normalized.length() > maxLength) {
                throw new IllegalArgumentException(
                        "Runtime Inbox " + field + "는 최대 " + maxLength + "자입니다.");
            }
            return normalized;
        }
    }

    /** 동일 deliveryId가 다른 Change/payload를 가리키는 durable journal 오염입니다. */
    public static final class IdentityConflictException extends IllegalStateException {
        private static final long serialVersionUID = 1L;

        IdentityConflictException(String message) {
            super(message);
        }
    }

    private final Path directory;

    public CpfRuntimeInstanceInboxStore(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory").toAbsolutePath().normalize();
        try {
            if (Files.isSymbolicLink(this.directory)) {
                throw new IllegalStateException(
                        "Runtime Inbox 디렉터리는 심볼릭 링크일 수 없습니다: " + this.directory);
            }
            Files.createDirectories(this.directory);
            if (Files.isSymbolicLink(this.directory)) {
                throw new IllegalStateException(
                        "Runtime Inbox 디렉터리는 심볼릭 링크일 수 없습니다: " + this.directory);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Runtime Inbox 디렉터리를 생성할 수 없습니다: " + this.directory, ex);
        }
    }

    public synchronized Optional<Entry> find(String deliveryId) {
        return read(path(deliveryId));
    }

    private Optional<Entry> read(Path path) {
        if (!Files.exists(path)) return Optional.empty();
        Properties p = new Properties();
        try (var in = Files.newInputStream(path)) {
            p.load(in);
            return Optional.of(fromProperties(p));
        } catch (IOException | RuntimeException ex) {
            throw new IllegalStateException("Runtime Inbox journal을 읽을 수 없습니다: " + path, ex);
        }
    }

    /** delivery identity를 검증한 durable journal을 조회합니다. */
    public synchronized Optional<Entry> find(CpfRuntimeDelivery delivery) {
        Objects.requireNonNull(delivery, "delivery");
        Optional<Entry> existing = find(delivery.deliveryId());
        existing.ifPresent(entry -> assertSameDelivery(entry, delivery));
        return existing;
    }

    public synchronized Entry prepare(CpfRuntimeDelivery delivery) {
        Optional<Entry> existing = find(delivery);
        if (existing.isPresent()) return existing.get();
        Entry prepared = new Entry(delivery.deliveryId(), delivery.changeId(), normalize(delivery.changeType()),
                delivery.desiredVersion(), delivery.payloadHash(), State.PREPARED, null, Instant.now());
        write(prepared);
        return prepared;
    }

    public synchronized Entry markApplied(CpfRuntimeDelivery delivery, String actualHash) {
        if (actualHash == null || actualHash.isBlank()) {
            throw new IllegalArgumentException("APPLIED Inbox에는 actualHash가 필요합니다.");
        }
        Entry existing = find(delivery).orElseThrow(() -> new IllegalStateException(
                "Runtime Inbox PREPARED journal 없이 APPLIED를 기록할 수 없습니다: " + delivery.deliveryId()));
        if (existing.state() == State.APPLIED) {
            if (!actualHash.equals(existing.actualHash())) {
                throw new IdentityConflictException(
                        "동일 deliveryId의 APPLIED actualHash가 다릅니다: " + delivery.deliveryId());
            }
            return existing;
        }
        Entry applied = new Entry(delivery.deliveryId(), delivery.changeId(), normalize(delivery.changeType()),
                delivery.desiredVersion(), delivery.payloadHash(), State.APPLIED, actualHash, Instant.now());
        write(applied);
        return applied;
    }

    /** 서버 ACK가 확정된 뒤 동일 delivery의 APPLIED 복구 journal을 제거합니다. */
    public synchronized void clearApplied(CpfRuntimeDelivery delivery) {
        Objects.requireNonNull(delivery, "delivery");
        Optional<Entry> entry = find(delivery);
        if (entry.isPresent() && entry.get().state() == State.APPLIED) {
            deleteJournal(delivery.deliveryId(), "APPLIED");
        }
    }

    /** 명시적 failure가 side effect 미발생을 보장한 경우에만 검증된 PREPARED를 제거합니다. */
    public synchronized void clearPrepared(CpfRuntimeDelivery delivery) {
        Optional<Entry> entry = find(delivery);
        if (entry.isPresent() && entry.get().state() == State.PREPARED) {
            deletePrepared(delivery.deliveryId());
        }
    }

    /** 기존 호출 호환입니다. 신규 Consumer는 delivery identity를 전달해야 합니다. */
    public synchronized void clearPrepared(String deliveryId) {
        Optional<Entry> entry = find(deliveryId);
        if (entry.isPresent() && entry.get().state() == State.PREPARED) {
            deletePrepared(deliveryId);
        }
    }

    private void deletePrepared(String deliveryId) {
        deleteJournal(deliveryId, "PREPARED");
    }

    private void deleteJournal(String deliveryId, String state) {
        try {
            Files.deleteIfExists(path(deliveryId));
            fsyncDirectory();
        } catch (IOException ex) {
            throw new IllegalStateException("Runtime Inbox " + state + " 삭제 실패: " + deliveryId, ex);
        }
    }

    public synchronized List<CpfRuntimeActualState> latestAppliedStates() {
        Map<String, Entry> latest = new LinkedHashMap<>();
        try (var stream = Files.list(directory)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".inbox"))
                    .forEach(path -> read(path).ifPresent(entry -> {
                        if (entry.state() != State.APPLIED) return;
                        latest.merge(entry.changeType(), entry,
                                (a, b) -> a.desiredVersion() >= b.desiredVersion() ? a : b);
                    }));
        } catch (IOException ex) {
            throw new IllegalStateException("Runtime Inbox 목록 조회 실패", ex);
        }
        ArrayList<CpfRuntimeActualState> states = new ArrayList<>();
        latest.values().stream().sorted(Comparator.comparing(Entry::changeType)).forEach(e -> states.add(e.toActualState()));
        return List.copyOf(states);
    }

    private Path path(String deliveryId) {
        if (deliveryId == null || !deliveryId.matches("[A-Za-z0-9._:-]{1,80}")) {
            throw new IllegalArgumentException("유효하지 않은 Runtime deliveryId입니다.");
        }
        String safe = Base64.getUrlEncoder().withoutPadding().encodeToString(deliveryId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return directory.resolve(safe + ".inbox").normalize();
    }

    private void write(Entry entry) {
        Path target = path(entry.deliveryId());
        Path temp;
        try {
            temp = Files.createTempFile(directory, target.getFileName().toString() + ".", ".tmp");
        } catch (IOException ex) {
            throw new IllegalStateException("Runtime Inbox 고유 임시 journal 생성 실패", ex);
        }
        Properties p = new Properties();
        p.setProperty("deliveryId", entry.deliveryId());
        p.setProperty("changeId", entry.changeId());
        p.setProperty("changeType", entry.changeType());
        p.setProperty("desiredVersion", Long.toString(entry.desiredVersion()));
        p.setProperty("payloadHash", empty(entry.payloadHash()));
        p.setProperty("state", entry.state().name());
        p.setProperty("actualHash", empty(entry.actualHash()));
        p.setProperty("updatedAt", entry.updatedAt().toString());
        try (OutputStream out = Files.newOutputStream(temp, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            p.store(out, "CPF Runtime Instance Inbox");
            out.flush();
        } catch (IOException ex) {
            try { Files.deleteIfExists(temp); } catch (IOException ignored) { }
            throw new IllegalStateException("Runtime Inbox 임시 journal 저장 실패", ex);
        }
        try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE)) {
            channel.force(true);
        } catch (IOException ex) {
            try { Files.deleteIfExists(temp); } catch (IOException ignored) { }
            throw new IllegalStateException("Runtime Inbox fsync 실패", ex);
        }
        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            fsyncDirectory();
        } catch (AtomicMoveNotSupportedException ex) {
            try { Files.deleteIfExists(temp); } catch (IOException ignored) { }
            throw new IllegalStateException("Runtime Inbox는 atomic rename을 지원하는 파일시스템이 필요합니다: " + directory, ex);
        } catch (IOException ex) {
            try { Files.deleteIfExists(temp); } catch (IOException ignored) { }
            throw new IllegalStateException("Runtime Inbox atomic replace 실패", ex);
        }
    }

    private void fsyncDirectory() {
        try (FileChannel channel = FileChannel.open(directory, StandardOpenOption.READ)) {
            channel.force(true);
        } catch (IOException | UnsupportedOperationException ex) {
            throw new IllegalStateException("Runtime Inbox directory fsync를 지원하지 않습니다: " + directory, ex);
        }
    }

    private Entry fromProperties(Properties p) {
        return new Entry(required(p, "deliveryId"), required(p, "changeId"), required(p, "changeType"),
                Long.parseLong(required(p, "desiredVersion")), p.getProperty("payloadHash", ""),
                State.valueOf(required(p, "state")), blankToNull(p.getProperty("actualHash")),
                Instant.parse(required(p, "updatedAt")));
    }

    private String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("Runtime Inbox 필수 필드 누락: " + key);
        return value;
    }

    private void assertSameDelivery(Entry entry, CpfRuntimeDelivery delivery) {
        String actualType = normalize(delivery.changeType());
        if (!entry.deliveryId().equals(delivery.deliveryId())
                || !entry.changeId().equals(delivery.changeId())
                || !entry.changeType().equals(actualType)
                || entry.desiredVersion() != delivery.desiredVersion()
                || !Objects.equals(entry.payloadHash(), delivery.payloadHash())) {
            throw new IdentityConflictException(
                    "동일 deliveryId가 다른 Runtime Change identity를 가리킵니다: " + delivery.deliveryId());
        }
    }

    private String normalize(String value) {
        String result = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        return result.startsWith("ROLLBACK:") ? result.substring("ROLLBACK:".length()) : result;
    }
    private String empty(String value) { return value == null ? "" : value; }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
}
