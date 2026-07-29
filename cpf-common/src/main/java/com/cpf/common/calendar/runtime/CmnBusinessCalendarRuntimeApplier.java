package com.cpf.common.calendar.runtime;

import com.cpf.common.calendar.CmnCalendarDay;
import com.cpf.common.calendar.CmnCalendarService;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

/** 공유 Calendar DB 정본을 Runtime이 실제 조회 가능한지 버전/영업일 상태로 검증합니다. */
public final class CmnBusinessCalendarRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "BUSINESS_CALENDAR";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final CmnCalendarService calendarService;

    public CmnBusinessCalendarRuntimeApplier(CmnCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override public String changeType() { return CHANGE_TYPE; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        JsonNode payload = readPayload(delivery);
        String calendarId = requiredText(payload, "calendarId");
        LocalDate businessDate = LocalDate.parse(requiredText(payload, "businessDate"));
        long expectedVersion = requiredLong(payload, "expectedVersion");
        boolean expectedBusinessDay = requiredBoolean(payload, "expectedBusinessDay");
        List<CmnCalendarDay> rows = calendarService.findRange(calendarId, businessDate, businessDate, 2);
        if (rows.size() != 1) {
            return CpfRuntimeApplyResult.failure(
                    "BUSINESS_CALENDAR_ROW_NOT_FOUND",
                    "요청한 Calendar 정본을 정확히 한 건 조회하지 못했습니다.");
        }
        CmnCalendarDay actual = rows.getFirst();
        if (actual.version() != expectedVersion || actual.businessDay() != expectedBusinessDay) {
            return CpfRuntimeApplyResult.failure(
                    "BUSINESS_CALENDAR_STATE_MISMATCH",
                    "Calendar 정본의 버전 또는 영업일 상태가 기대값과 일치하지 않습니다.");
        }
        return CpfRuntimeApplyResult.success(delivery.payloadHash());
    }

    private JsonNode readPayload(CpfRuntimeDelivery delivery) {
        try {
            return OBJECT_MAPPER.readTree(delivery.payload().canonicalJson());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("검증된 Runtime payload를 읽을 수 없습니다.", exception);
        }
    }

    private String requiredText(JsonNode payload, String name) {
        JsonNode value = payload.get(name);
        if (value == null || !value.isTextual() || value.textValue().isBlank()) {
            throw new IllegalArgumentException(name + "는 필수 문자열입니다.");
        }
        return value.textValue().trim();
    }

    private long requiredLong(JsonNode payload, String name) {
        JsonNode value = payload.get(name);
        if (value == null || !value.isIntegralNumber()) {
            throw new IllegalArgumentException(name + "는 필수 숫자입니다.");
        }
        return value.longValue();
    }

    private boolean requiredBoolean(JsonNode payload, String name) {
        JsonNode value = payload.get(name);
        if (value == null || !value.isBoolean()) {
            throw new IllegalArgumentException(name + "는 필수 boolean입니다.");
        }
        return value.booleanValue();
    }
}
