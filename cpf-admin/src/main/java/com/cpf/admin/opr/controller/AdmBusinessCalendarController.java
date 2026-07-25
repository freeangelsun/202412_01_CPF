package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.common.calendar.CmnCalendarDay;
import com.cpf.common.calendar.CmnCalendarService;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.core.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM 영업일/휴일 관리 API입니다.
 *
 * <p>ADM은 고객 Calendar DB를 직접 소유하지 않고 {@link CmnCalendarService} 공개 계약을 통해
 * 조회/변경합니다. Batch/Scheduler/업무 Domain도 동일 계약을 사용하므로 관리값과 실행값이 분리되지 않습니다.</p>
 */
@RestController
@RequestMapping("/adm/api/business-calendars")
@Tag(name = "ADM-BusinessCalendar", description = "영업일/휴일 Calendar 관리 및 수행일 계산 API")
public class AdmBusinessCalendarController extends com.cpf.admin.common.base.AdmBaseController {
    private final CmnCalendarService calendarService;
    private final AdmAuditLogService auditLogService;

    public AdmBusinessCalendarController(CmnCalendarService calendarService, AdmAuditLogService auditLogService) {
        this.calendarService = calendarService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/{calendarId}/days")
    @CpfOnlineTransaction(id = "OADMCL0001", name = "ADMBusinessCalendarDays")
    @Operation(operationId = "admCalendarFindDays", summary = "영업일 Override 조회")
    public ResponseEntity<Map<String,Object>> findDays(
            @PathVariable String calendarId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "366") int limit) {
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("calendarId", calendarId);
        result.put("writable", calendarService.writable());
        result.put("items", calendarService.findRange(calendarId, from, to, limit));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{calendarId}/resolve")
    @CpfOnlineTransaction(id = "OADMCL0002", name = "ADMBusinessCalendarResolve")
    @Operation(operationId = "admCalendarResolveDate", summary = "영업일 여부/기준일 계산")
    public ResponseEntity<Map<String,Object>> resolve(
            @PathVariable String calendarId,
            @RequestParam LocalDate date,
            @RequestParam(defaultValue = "1") int offset) {
        return ResponseEntity.ok(Map.of(
                "calendarId", calendarId,
                "date", date,
                "businessDay", calendarService.isBusinessDay(calendarId, date),
                "shiftedBusinessDate", calendarService.shiftBusinessDay(calendarId, date, offset)));
    }

    @PutMapping("/{calendarId}/days/{businessDate}")
    @CpfOnlineTransaction(id = "OADMCL0003", name = "ADMBusinessCalendarSave")
    @Operation(
            operationId = "admCalendarSaveDay",
            summary = "영업일/휴일 Override 저장",
            description = "expectedVersion을 사용해 동시 변경을 방지합니다.")
    public ResponseEntity<CmnCalendarDay> save(
            @PathVariable String calendarId,
            @PathVariable LocalDate businessDate,
            @RequestParam(defaultValue = "0") long expectedVersion,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            examples = @ExampleObject(value = """
                                    {"businessDay":false,"dayType":"HOLIDAY","institutionCode":"BANK","reason":"정기 휴일"}
                                    """)))
            @RequestBody SaveDayRequest body,
            HttpServletRequest request) {
        String operatorId = requireOperator(request);
        CmnCalendarDay saved = calendarService.save(new CmnCalendarDay(
                calendarId,
                businessDate,
                body.businessDay(),
                defaultText(body.dayType(), body.businessDay() ? "BUSINESS" : "HOLIDAY"),
                defaultText(body.institutionCode(), ""),
                defaultText(body.reason(), ""),
                expectedVersion), expectedVersion);
        audit(request, operatorId, "BUSINESS_CALENDAR_SAVE", calendarId + ":" + businessDate, saved);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{calendarId}/days/{businessDate}")
    @CpfOnlineTransaction(id = "OADMCL0004", name = "ADMBusinessCalendarDelete")
    @Operation(operationId = "admCalendarDeleteDay", summary = "영업일 Override 삭제")
    public ResponseEntity<Void> delete(
            @PathVariable String calendarId,
            @PathVariable LocalDate businessDate,
            @Parameter(description = "현재 version") @RequestParam long expectedVersion,
            HttpServletRequest request) {
        String operatorId = requireOperator(request);
        calendarService.delete(calendarId, businessDate, expectedVersion);
        audit(request, operatorId, "BUSINESS_CALENDAR_DELETE", calendarId + ":" + businessDate, Map.of("expectedVersion", expectedVersion));
        return ResponseEntity.noContent().build();
    }

    private void audit(HttpServletRequest request, String operatorId, String action, String id, Object after) {
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                operatorId,
                action,
                "cmn_business_calendar",
                id,
                "영업일 관리",
                "",
                String.valueOf(after),
                "ADM Calendar",
                request.getRemoteAddr());
    }


    /** 인증 Filter가 검증한 운영자 ID가 없으면 변경성 작업을 fail-closed합니다. */
    private String requireOperator(HttpServletRequest request) {
        Object operator = request.getAttribute("adm.operatorId");
        if (operator instanceof String value && !value.isBlank()) {
            return value.trim();
        }
        throw new IllegalStateException("ADM 운영자 식별자가 없어 영업일 변경을 수행할 수 없습니다.");
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    /** 영업일 Override 저장 요청입니다. */
    public record SaveDayRequest(
            boolean businessDay,
            String dayType,
            String institutionCode,
            String reason) {
    }
}
