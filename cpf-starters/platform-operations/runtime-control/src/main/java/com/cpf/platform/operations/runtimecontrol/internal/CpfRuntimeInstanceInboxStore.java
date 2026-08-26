package com.cpf.platform.operations.runtimecontrol.internal;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeActualState;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;

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
        CpfRuntimeActualState toActualState() {
            return new CpfRuntimeActualState(changeType, desiredVersion, actualHash, deliveryId);
        }
    }

    private final Path directory;

    public CpfRuntimeInstanceInboxStore(Path directory) {
        this.directory = directory.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directory);
        } catch (IOException ex) {
            throw new IllegalStateException("Runtime Inbox 디렉터리를 생성할 수 없습니다: " + this.directory, ex);
        }
    }

    public synchronized Optional<Entry> find(String deliveryId) {
        Path path = path(deliveryId);
        if (!Files.exists(path)) return Optional.empty();
        Properties p = new Properties();
        try (var in = Files.newInputStream(path)) {
            p.load(in);
            return Optional.of(fromProperties(p));
        } catch (IOException | RuntimeException ex) {
            throw new IllegalStateException("Runtime Inbox journal을 읽을 수 없습니다: " + path, ex);
        }
    }

    public synchronized Entry prepare(CpfRuntimeDelivery delivery) {
        Optional<Entry> existing = find(delivery.deliveryId());
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
        Entry applied = new Entry(delivery.deliveryId(), delivery.changeId(), normalize(delivery.changeType()),
                delivery.desiredVersion(), delivery.payloadHash(), State.APPLIED, actualHash, Instant.now());
        write(applied);
        return applied;
    }

    /** 명시적 failure가 side effect 미발생을 보장한 경우에만 PREPARED를 제거합니다. */
    public synchronized void clearPrepared(String deliveryId) {
        Optional<Entry> entry = find(deliveryId);
        if (entry.isPresent() && entry.get().state() == State.PREPARED) {
            try {
                Files.deleteIfExists(path(deliveryId));
                fsyncDirectory();
            } catch (IOException ex) {
                throw new IllegalStateException("Runtime Inbox PREPARED 삭제 실패: " + deliveryId, ex);
            }
        }
    }

    public synchronized List<CpfRuntimeActualState> latestAppliedStates() {
        Map<String, Entry> latest = new LinkedHashMap<>();
        try (var stream = Files.list(directory)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".inbox"))
                    .forEach(path -> {
                        try {
                            String file = path.getFileName().toString();
                            find(file.substring(0, file.length() - 6)).ifPresent(entry -> {
                                if (entry.state() != State.APPLIED) return;
                                latest.merge(entry.changeType(), entry,
                                        (a, b) -> a.desiredVersion() >= b.desiredVersion() ? a : b);
                            });
                        } catch (RuntimeException ignored) {
                            // 개별 손상 journal은 startup을 실패시켜야 하므로 아래 정렬 전에 다시 읽힙니다.
                            throw ignored;
                        }
                    });
        } catch (IOException ex) {
            throw new IllegalStateException("Runtime Inbox 목록 조회 실패", ex);
        }
        ArrayList<CpfRuntimeActualState> states = new ArrayList<>();
        latest.values().stream().sorted(Comparator.comparing(value -> value.changeType())).forEach(e -> states.add(e.toActualState()));
        return List.copyOf(states);
    }

    private Path path(String deliveryId) {
        if (deliveryId == null || !deliveryId.matches("[A-Za-z0-9._:-]{1,180}")) {
            throw new IllegalArgumentException("유효하지 않은 Runtime deliveryId입니다.");
        }
        String safe = Base64.getUrlEncoder().withoutPadding().encodeToString(deliveryId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return directory.resolve(safe + ".inbox").normalize();
    }

    private void write(Entry entry) {
        Path target = path(entry.deliveryId());
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
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
            throw new IllegalStateException("Runtime Inbox 임시 journal 저장 실패", ex);
        }
        try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE)) {
            channel.force(true);
        } catch (IOException ex) {
            throw new IllegalStateException("Runtime Inbox fsync 실패", ex);
        }
        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            fsyncDirectory();
        } catch (AtomicMoveNotSupportedException ex) {
            try { Files.deleteIfExists(temp); } catch (IOException ignored) { }
            throw new IllegalStateException("Runtime Inbox는 atomic rename을 지원하는 파일시스템이 필요합니다: " + directory, ex);
        } catch (IOException ex) {
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

    private String normalize(String value) {
        String result = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        return result.startsWith("ROLLBACK:") ? result.substring("ROLLBACK:".length()) : result;
    }
    private String empty(String value) { return value == null ? "" : value; }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
}
