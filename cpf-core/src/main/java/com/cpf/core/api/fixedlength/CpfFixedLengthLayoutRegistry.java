package com.cpf.core.api.fixedlength;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * layout ID와 version별 immutable layout을 보관하는 thread-safe registry입니다.
 */
public final class CpfFixedLengthLayoutRegistry {
    private final Map<LayoutKey, CpfFixedLengthLayout> layouts = new ConcurrentHashMap<>();

    public void register(CpfFixedLengthLayout layout) {
        if (layout == null) {
            throw new IllegalArgumentException("고정길이 layout은 필수입니다.");
        }
        layouts.put(new LayoutKey(layout.layoutId(), layout.version()), layout);
    }

    public void register(String layoutId, CpfFixedLengthLayout layout) {
        if (layout == null) {
            throw new IllegalArgumentException("고정길이 layout은 필수입니다.");
        }
        layouts.put(new LayoutKey(layoutId, layout.version()), layout);
    }

    public Optional<CpfFixedLengthLayout> find(String layoutId, String version) {
        if (layoutId == null || layoutId.isBlank() || version == null || version.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(layouts.get(new LayoutKey(layoutId, version)));
    }

    public CpfFixedLengthLayout require(String layoutId, String version) {
        return find(layoutId, version)
                .orElseThrow(() -> new CpfFixedLengthException(
                        "등록된 고정길이 layout이 없습니다.",
                        java.util.List.of(new CpfFixedLengthError(
                                "layoutId",
                                "CPF_FIXED_LAYOUT_NOT_FOUND",
                                "요청한 layout ID와 version이 registry에 없습니다."))));
    }

    public int size() {
        return layouts.size();
    }

    private record LayoutKey(String layoutId, String version) {
        private LayoutKey {
            if (layoutId == null || layoutId.isBlank()) {
                throw new IllegalArgumentException("고정길이 layout ID는 필수입니다.");
            }
            if (version == null || version.isBlank()) {
                throw new IllegalArgumentException("고정길이 layout version은 필수입니다.");
            }
            layoutId = layoutId.trim();
            version = version.trim();
        }
    }
}
