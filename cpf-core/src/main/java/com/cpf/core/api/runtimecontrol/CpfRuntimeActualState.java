package com.cpf.core.api.runtimecontrol;

import java.util.Locale;

/** Runtime Agent가 durable Inbox를 근거로 재보고하는 기능별 실제 상태입니다. */
public record CpfRuntimeActualState(
        String changeType,
        long actualVersion,
        String actualHash,
        String sourceDeliveryId) {

    public CpfRuntimeActualState {
        changeType = require(changeType, "changeType", 80).toUpperCase(Locale.ROOT);
        if (actualVersion < 0L) {
            throw new IllegalArgumentException("actualVersion은 0 이상이어야 합니다.");
        }
        actualHash = require(actualHash, "actualHash", 64);
        sourceDeliveryId = require(sourceDeliveryId, "sourceDeliveryId", 80);
    }

    private static String require(String value, String name, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + "가 필요합니다.");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(name + "는 최대 " + maxLength + "자입니다.");
        }
        return normalized;
    }
}
