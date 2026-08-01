package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmBreakGlassService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.logging.CpfTransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 긴급 Break-glass 요청·승인·종료를 사유와 불변 감사로 통제합니다. */
@RestController
@RequestMapping("/adm/api/break-glass")
@Tag(name="ADM-BreakGlass", description="TTL/scope/post-review 기반 비상 권한 세션")
public class AdmBreakGlassController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmBreakGlassService service;
    private final AdmAuditLogService audit;
    public AdmBreakGlassController(AdmBreakGlassService service, AdmAuditLogService audit) { this.service=service; this.audit=audit; }

    @GetMapping @CpfOnlineTransaction(id="OADMBG0001",name="ADMBreakGlassList") @Operation(operationId="admBreakGlassFindSessions", summary="Break-glass 세션 목록")
    public ResponseEntity<List<Map<String,Object>>> list(@RequestParam(required=false) String status,@RequestParam(defaultValue="100") int limit){return ResponseEntity.ok(service.list(status,limit));}

    @PostMapping @CpfOnlineTransaction(id="OADMBG0002",name="ADMBreakGlassOpen") @Operation(operationId="admBreakGlassOpenSession", summary="Break-glass 세션 발급",
            description="scope와 TTL이 제한된 세션만 발급합니다. 전역 권한 우회는 제공하지 않습니다.")
    public ResponseEntity<Map<String,Object>> open(@RequestBody Map<String,Object> body,HttpServletRequest request){String user=operator(request);Map<String,Object> result=service.open(user,
            String.valueOf(body.get("scopeType")),String.valueOf(body.get("scopeValue")),String.valueOf(body.get("reason")),number(body.get("ttlMinutes"),15));audit(request,user,
            "BREAK_GLASS_OPEN","adm_break_glass_session",String.valueOf(result.get("sessionId")),String.valueOf(body.get("reason")),result);return ResponseEntity.ok(result);}

    @PostMapping("/{sessionId}/close") @CpfOnlineTransaction(id="OADMBG0003",name="ADMBreakGlassClose") @Operation(operationId="admBreakGlassCloseSession", summary="Break-glass 세션 종료")
    public ResponseEntity<Map<String,Object>> close(@PathVariable String sessionId,@RequestBody Map<String,Object> body,HttpServletRequest request){String user=operator(request);Map<String,
            Object> result=service.close(sessionId,user,String.valueOf(body.get("reason")));audit(request,user,"BREAK_GLASS_CLOSE","adm_break_glass_session",sessionId,
            String.valueOf(body.get("reason")),result);return ResponseEntity.ok(result);}

    @PostMapping("/{sessionId}/review") @CpfOnlineTransaction(id="OADMBG0004",name="ADMBreakGlassReview") @Operation(operationId="admBreakGlassReviewSession", summary="Break-glass 사후검토")
    public ResponseEntity<Map<String,Object>> review(@PathVariable String sessionId,@RequestBody Map<String,Object> body,HttpServletRequest request){String user=operator(request);Map<String,
            Object> result=service.review(sessionId,user,String.valueOf(body.get("status")),String.valueOf(body.get("reason")));audit(request,user,"BREAK_GLASS_REVIEW",
            "adm_break_glass_session",sessionId,String.valueOf(body.get("reason")),result);return ResponseEntity.ok(result);}

    private static int number(Object value,int fallback){if(value instanceof Number n)return n.intValue();try{return value==null?fallback:Integer.parseInt(String.valueOf(value));}catch(Exception e){return fallback;}}
    private String operator(HttpServletRequest req){Object value=req.getAttribute("adm.operatorId");if(value instanceof String s&&!s.isBlank())return s;throw new IllegalStateException("ADM operator context가 필요합니다.");}
    private void audit(HttpServletRequest req,String user,String action,String type,String id,String reason,Object after){audit.record(CpfTransactionContext.transactionId(),user,action,type,
            id,reason,"",String.valueOf(after),"break-glass 상태 변경",req.getRemoteAddr());}
}
