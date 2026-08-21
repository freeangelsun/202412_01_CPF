package com.cpf.admin.opr.controller;

import org.springframework.web.bind.annotation.RestController;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.dto.AdmBreakGlassOpenRequest;
import com.cpf.admin.opr.dto.AdmBreakGlassReviewRequest;
import com.cpf.admin.opr.dto.AdmReasonRequest;
import com.cpf.admin.opr.service.AdmBreakGlassService;
import com.cpf.core.api.context.CpfContexts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

    @GetMapping@Operation(operationId="admBreakGlassFindSessions", summary="Break-glass 세션 목록")
    public ResponseEntity<List<Map<String,Object>>> list(@RequestParam(required=false) String status,@RequestParam(defaultValue="100") int limit){return ResponseEntity.ok(service.list(status,limit));}

    @PostMapping@Operation(operationId="admBreakGlassOpenSession", summary="Break-glass 세션 발급",
            description="scope와 TTL이 제한된 세션만 발급합니다. 전역 권한 우회는 제공하지 않습니다.")
    public ResponseEntity<Map<String,Object>> open(@Valid @RequestBody AdmBreakGlassOpenRequest body,HttpServletRequest request){String user=operator(request);Map<String,Object> result=service.open(user,
            body.scopeType(),body.scopeValue(),body.reason(),body.ttlMinutes());audit(request,user,
            "BREAK_GLASS_OPEN","adm_break_glass_session",String.valueOf(result.get("sessionId")),body.reason(),result);return ResponseEntity.ok(result);}

    @PostMapping("/{sessionId}/close")@Operation(operationId="admBreakGlassCloseSession", summary="Break-glass 세션 종료")
    public ResponseEntity<Map<String,Object>> close(@PathVariable String sessionId,@Valid @RequestBody AdmReasonRequest body,HttpServletRequest request){String user=operator(request);Map<String,
            Object> result=service.close(sessionId,user,body.reason());audit(request,user,"BREAK_GLASS_CLOSE","adm_break_glass_session",sessionId,
            body.reason(),result);return ResponseEntity.ok(result);}

    @PostMapping("/{sessionId}/review")@Operation(operationId="admBreakGlassReviewSession", summary="Break-glass 사후검토")
    public ResponseEntity<Map<String,Object>> review(@PathVariable String sessionId,@Valid @RequestBody AdmBreakGlassReviewRequest body,HttpServletRequest request){String user=operator(request);Map<String,
            Object> result=service.review(sessionId,user,body.status(),body.reason());audit(request,user,"BREAK_GLASS_REVIEW",
            "adm_break_glass_session",sessionId,body.reason(),result);return ResponseEntity.ok(result);}

    private String operator(HttpServletRequest req){Object value=req.getAttribute("adm.operatorId");if(value instanceof String s&&!s.isBlank())return s;throw new IllegalStateException("ADM operator context가 필요합니다.");}
    private void audit(HttpServletRequest req,String user,String action,String type,String id,String reason,Object after){audit.record(CpfContexts.transactionId(),user,action,type,
            id,reason,"",String.valueOf(after),"break-glass 상태 변경",req.getRemoteAddr());}
}
