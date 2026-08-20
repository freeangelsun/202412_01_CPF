package com.cpf.integration.fixedlength.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * layout ID와 version별 immutable layout snapshot을 보관하는 thread-safe registry입니다.
 * 같은 layout version은 immutable하며 전체 snapshot은 원자적으로 교체됩니다.
 */
public final class CpfFixedLengthLayoutRegistry {
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>(Snapshot.empty());

    public void register(CpfFixedLengthLayout layout) {
        upsert(snapshot.get().version() + 1L, layout, "");
    }

    /** register 작업을 CPF 표준 계약에 따라 수행한다. */
    public void register(String layoutId, CpfFixedLengthLayout layout) {
        if (layout == null) {
            throw new IllegalArgumentException("고정길이 layout은 필수입니다.");
        }
        CpfFixedLengthLayout normalized = layoutId == null || layoutId.isBlank()
                ? layout
                : new CpfFixedLengthLayout(layoutId, layout.version(), layout.charset(),
                        layout.totalLength(), layout.fields(), layout.groups());
        upsert(snapshot.get().version() + 1L, normalized, "");
    }

    /** upsert 작업을 CPF 표준 계약에 따라 수행한다. */
    public Snapshot upsert(long registryVersion, CpfFixedLengthLayout layout, String expectedHash) {
        if (layout == null) {
            throw new IllegalArgumentException("고정길이 layout은 필수입니다.");
        }
        Snapshot current = snapshot.get();
        Map<LayoutKey, CpfFixedLengthLayout> next = new LinkedHashMap<>(current.layouts());
        LayoutKey key = new LayoutKey(layout.layoutId(), layout.version());
        CpfFixedLengthLayout existing = next.get(key);
        if (existing != null && !canonical(existing).equals(canonical(layout))) {
            throw new IllegalArgumentException("동일 layout version은 변경할 수 없습니다. 새 version을 사용하십시오.");
        }
        next.put(key, layout);
        return replaceSnapshot(registryVersion, next.values(), expectedHash, Compatibility.BACKWARD);
    }

    /** replaceSnapshot 작업을 CPF 표준 계약에 따라 수행한다. */
    public Snapshot replaceSnapshot(
            long registryVersion,
            Collection<CpfFixedLengthLayout> layouts,
            String expectedHash,
            Compatibility compatibility) {
        if (registryVersion < 0L) {
            throw new IllegalArgumentException("registryVersion은 0 이상이어야 합니다.");
        }
        Map<LayoutKey, CpfFixedLengthLayout> next = normalize(layouts);
        String hash = hash(next);
        if (expectedHash != null && !expectedHash.isBlank()
                && !expectedHash.trim().equalsIgnoreCase(hash)) {
            throw new IllegalArgumentException("고정길이 registry hash가 일치하지 않습니다.");
        }
        while (true) {
            Snapshot current = snapshot.get();
            if (registryVersion < current.version()) {
                throw new IllegalArgumentException("고정길이 registry version 역행은 허용되지 않습니다.");
            }
            validateCompatibility(current.layouts(), next,
                    compatibility == null ? Compatibility.BACKWARD : compatibility);
            Snapshot replacement = new Snapshot(registryVersion, hash, next);
            if (snapshot.compareAndSet(current, replacement)) {
                return replacement;
            }
        }
    }

    /** find 작업을 CPF 표준 계약에 따라 수행한다. */
    public Optional<CpfFixedLengthLayout> find(String layoutId, String version) {
        if (layoutId == null || layoutId.isBlank() || version == null || version.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(snapshot.get().layouts().get(new LayoutKey(layoutId, version)));
    }

    /** require 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfFixedLengthLayout require(String layoutId, String version) {
        return find(layoutId, version)
                .orElseThrow(() -> new CpfFixedLengthException(
                        "등록된 고정길이 layout이 없습니다.",
                        java.util.List.of(new CpfFixedLengthError(
                                "layoutId", "CPF_FIXED_LAYOUT_NOT_FOUND",
                                "요청한 layout ID와 version이 registry에 없습니다."))));
    }

    /** size 작업을 CPF 표준 계약에 따라 수행한다. */
    public int size() { return snapshot.get().layouts().size(); }
    public Snapshot snapshot() { return snapshot.get(); }

    private static Map<LayoutKey, CpfFixedLengthLayout> normalize(Collection<CpfFixedLengthLayout> layouts) {
        if (layouts == null) throw new IllegalArgumentException("layout snapshot은 필수입니다.");
        List<CpfFixedLengthLayout> sorted = new ArrayList<>(layouts);
        sorted.sort(Comparator.comparing(CpfFixedLengthLayout::layoutId)
                .thenComparing(CpfFixedLengthLayout::version));
        Map<LayoutKey, CpfFixedLengthLayout> result = new LinkedHashMap<>();
        for (CpfFixedLengthLayout layout : sorted) {
            if (layout == null) throw new IllegalArgumentException("null layout은 허용되지 않습니다.");
            LayoutKey key = new LayoutKey(layout.layoutId(), layout.version());
            CpfFixedLengthLayout old = result.putIfAbsent(key, layout);
            if (old != null) throw new IllegalArgumentException("layout ID/version이 중복되었습니다.");
        }
        return Map.copyOf(result);
    }

    private static void validateCompatibility(
            Map<LayoutKey, CpfFixedLengthLayout> current,
            Map<LayoutKey, CpfFixedLengthLayout> next,
            Compatibility mode) {
        if (mode == Compatibility.NONE || current.isEmpty()) return;
        if (mode == Compatibility.BACKWARD || mode == Compatibility.FULL) {
            for (Map.Entry<LayoutKey, CpfFixedLengthLayout> entry : current.entrySet()) {
                CpfFixedLengthLayout candidate = next.get(entry.getKey());
                if (candidate == null || !canonical(candidate).equals(canonical(entry.getValue()))) {
                    throw new IllegalArgumentException("BACKWARD 호환성 위반: 기존 layout version 제거/변경");
                }
            }
        }
        if (mode == Compatibility.FORWARD || mode == Compatibility.FULL) {
            for (Map.Entry<LayoutKey, CpfFixedLengthLayout> entry : next.entrySet()) {
                CpfFixedLengthLayout old = current.get(entry.getKey());
                if (old != null && !canonical(old).equals(canonical(entry.getValue()))) {
                    throw new IllegalArgumentException("FORWARD 호환성 위반: 동일 version 계약 변경");
                }
            }
        }
    }

    private static String hash(Map<LayoutKey, CpfFixedLengthLayout> layouts) {
        String canonical = layouts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> canonical(e.getValue()))
                .reduce("", (a, b) -> a + "\n" + b);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String canonical(CpfFixedLengthLayout layout) {
        return layout.layoutId() + '|' + layout.version() + '|' + layout.charset().name() + '|'
                + layout.totalLength() + '|' + layout.fields() + '|' + layout.groups();
    }

    /** Compatibility 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public enum Compatibility { NONE, BACKWARD, FORWARD, FULL }

    /** Fixed-length Layout Registry의 현재 Layout 집합과 version을 전달하는 불변 Snapshot입니다. */
    public record Snapshot(long version, String hash, Map<LayoutKey, CpfFixedLengthLayout> layouts) {
        public Snapshot {
            hash = hash == null ? "" : hash;
            layouts = layouts == null ? Map.of() : Map.copyOf(layouts);
        }
        private static Snapshot empty() { return new Snapshot(0L, CpfFixedLengthLayoutRegistry.hash(Map.of()), Map.of()); }
        /** values 작업을 CPF 표준 계약에 따라 수행한다. */
        public Collection<CpfFixedLengthLayout> values() { return layouts.values(); }
    }

    /** 전문 Layout을 message type과 version 조합으로 유일하게 식별하는 Registry key입니다. */
    public record LayoutKey(String layoutId, String version) implements Comparable<LayoutKey> {
        public LayoutKey {
            if (layoutId == null || layoutId.isBlank()) throw new IllegalArgumentException("layout ID는 필수입니다.");
            if (version == null || version.isBlank()) throw new IllegalArgumentException("layout version은 필수입니다.");
            layoutId = layoutId.trim(); version = version.trim();
        }
        @Override public int compareTo(LayoutKey other) {
            int result = layoutId.compareTo(other.layoutId);
            return result != 0 ? result : version.compareTo(other.version);
        }
    }
}
