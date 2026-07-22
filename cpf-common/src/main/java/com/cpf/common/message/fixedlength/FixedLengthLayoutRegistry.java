package com.cpf.common.message.fixedlength;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 전문 ID와 layout 정의를 연결하는 in-memory registry skeleton입니다.
 */
public class FixedLengthLayoutRegistry {
    private final Map<String, FixedLengthLayoutSpec> layouts = new ConcurrentHashMap<>();

    public void register(String messageId, FixedLengthLayoutSpec layout) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("전문 ID는 필수입니다.");
        }
        if (layout == null) {
            throw new IllegalArgumentException("layout은 필수입니다. messageId=" + messageId);
        }
        layouts.put(messageId.trim(), layout);
    }

    public Optional<FixedLengthLayoutSpec> find(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(layouts.get(messageId.trim()));
    }

    public FixedLengthLayoutSpec require(String messageId) {
        return find(messageId)
                .orElseThrow(() -> new FixedLengthMessageException(
                        "등록된 고정길이 전문 layout이 없습니다. messageId=" + messageId,
                        java.util.List.of(new FixedLengthMessageError("messageId", "FIXED_LAYOUT_NOT_FOUND", "전문 layout을 찾을 수 없습니다."))));
    }
}
