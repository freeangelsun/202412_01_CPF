package com.cpf.admin.opr.controller;

import org.springframework.web.bind.annotation.RestController;
import com.cpf.admin.common.base.AdmBaseController;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.common.calendar.CmnCalendarConflictException;
import com.cpf.common.calendar.CmnCalendarDay;
import com.cpf.common.calendar.CmnCalendarService;
import com.cpf.core.api.context.CpfContexts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** ADM 영업일/휴일 관리 API. CMN Owner 계약만 사용합니다. */
@RestController
@RequestMapping("/adm/api/business-calendars")
@Tag(name="ADM-BusinessCalendar",description="영업일/휴일 Calendar 관리 및 수행일 계산 API")
public class AdmBusinessCalendarController extends AdmBaseController {
    private final CmnCalendarService calendarService; private final AdmAuditLogService auditLogService;
    public AdmBusinessCalendarController(CmnCalendarService s,AdmAuditLogService a){this.calendarService=s;this.auditLogService=a;}

    @GetMapping("/{calendarId}/days")    @Operation(operationId="admCalendarFindDays",summary="영업일 Override 조회")
    public ResponseEntity<Map<String,Object>> findDays(@PathVariable String calendarId,@RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to,@RequestParam(defaultValue="366") int limit){
        Map<String,Object> r=new LinkedHashMap<>();r.put("calendarId",calendarId);r.put("writable",calendarService.writable());r.put("productMode",calendarService.productMode());
                r.put("items",calendarService.findRange(calendarId,from,to,limit));return ResponseEntity.ok(r);
    }
    @GetMapping("/{calendarId}/resolve")    @Operation(operationId="admCalendarResolveDate",summary="영업일 여부/기준일 계산")
    public ResponseEntity<Map<String,Object>> resolve(@PathVariable String calendarId,@RequestParam LocalDate date,@RequestParam(defaultValue="1") int offset){return
            ResponseEntity.ok(Map.of("calendarId",calendarId,"date",date,"businessDay",calendarService.isBusinessDay(calendarId,date),"shiftedBusinessDate",calendarService.shiftBusinessDay(calendarId,date,offset)));}

    @PutMapping("/{calendarId}/days/{businessDate}")    @Operation(operationId="admCalendarSaveDay",summary="영업일/휴일 Override 저장",description="업무 사유와 감사 사유를 분리하며 expectedVersion CAS를 사용합니다.")
    public ResponseEntity<CmnCalendarDay> save(@PathVariable String calendarId,@PathVariable LocalDate businessDate,@RequestParam(defaultValue="0") long expectedVersion,@RequestBody
            SaveDayRequest body,HttpServletRequest request){
        String operator=requireOperator(request);String auditReason=auditLogService.requireReason(body.auditReason());
        CmnCalendarDay before=calendarService.findDay(calendarId,businessDate).orElse(null);
        CmnCalendarDay day=new CmnCalendarDay(calendarId,businessDate,body.businessDay(),defaultText(body.dayType(),body.businessDay()?"BUSINESS":"HOLIDAY"),
                defaultText(body.institutionCode(),""),defaultText(body.reason(),""),expectedVersion);
        CmnCalendarDay saved=auditLogService.executeAudited(CpfContexts.transactionId(),operator,"BUSINESS_CALENDAR_SAVE","cmn_business_calendar",calendarId+":"+businessDate,
                auditReason,before==null?null:String.valueOf(before),clientIp(request),()->calendarService.save(day,expectedVersion,operator),String::valueOf);
        return ResponseEntity.ok(saved);
    }
    @DeleteMapping("/{calendarId}/days/{businessDate}")    @Operation(operationId="admCalendarDeleteDay",summary="영업일 Override 삭제")
    public ResponseEntity<Void> delete(@PathVariable String calendarId,@PathVariable LocalDate businessDate,@RequestParam long expectedVersion,@RequestParam String auditReason,HttpServletRequest request){
        String operator=requireOperator(request);String why=auditLogService.requireReason(auditReason);
        CmnCalendarDay before=calendarService.findDay(calendarId,businessDate).orElse(null);
        auditLogService.executeAudited(CpfContexts.transactionId(),operator,"BUSINESS_CALENDAR_DELETE","cmn_business_calendar",calendarId+":"+businessDate,why,
                before==null?null:String.valueOf(before),clientIp(request),()->{calendarService.delete(calendarId,businessDate,expectedVersion,operator);return Boolean.TRUE;},r->"deleted=true;after=null");
        return ResponseEntity.noContent().build();
    }
    @ExceptionHandler(CmnCalendarConflictException.class)
    public ResponseEntity<Map<String,Object>> conflict(CmnCalendarConflictException ex){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errorCode",ex.type().name(),"message",ex.getMessage()));}
    private String defaultText(String v,String f){return v==null||v.isBlank()?f:v.trim();}
    public record SaveDayRequest(boolean businessDay,String dayType,String institutionCode,String reason,String auditReason){}
}
