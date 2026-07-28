package com.cpf.common.calendar.runtime;

import com.cpf.common.calendar.CmnCalendarDay;
import com.cpf.common.calendar.CmnCalendarService;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** 공유 Calendar DB 정본을 Runtime이 실제 조회 가능한지 버전/영업일 상태로 검증합니다. */
public final class CmnBusinessCalendarRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "BUSINESS_CALENDAR";
    private final CmnCalendarService calendarService;

    public CmnBusinessCalendarRuntimeApplier(CmnCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override public String changeType() { return CHANGE_TYPE; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        Map<String, Object> payload = delivery.payload();
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

    private String requiredText(Map<String, Object> payload, String name) {
        Object value = payload.get(name);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException(name + "는 필수 문자열입니다.");
        }
        return text.trim();
    }

    private long requiredLong(Map<String, Object> payload, String name) {
        Object value = payload.get(name);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException(name + "는 필수 숫자입니다.");
        }
        return number.longValue();
    }

    private boolean requiredBoolean(Map<String, Object> payload, String name) {
        Object value = payload.get(name);
        if (!(value instanceof Boolean bool)) {
            throw new IllegalArgumentException(name + "는 필수 boolean입니다.");
        }
        return bool;
    }
}
