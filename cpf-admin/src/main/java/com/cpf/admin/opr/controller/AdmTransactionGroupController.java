package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmTransactionGroupService;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.api.context.CpfContexts;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * transactionId 기준 복합 거래 그룹 조회 API입니다.
 */
@RestController
@RequestMapping("/adm/api/transaction-groups")
@Tag(name = "ADM-TransactionGroup", description = "transactionId 그룹, 구간, 타임라인, 헤더, 외부 호출 조회 API")
public class AdmTransactionGroupController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmTransactionGroupService transactionGroupService;
    private final AdmAuditLogService auditLogService;

    public AdmTransactionGroupController(AdmTransactionGroupService transactionGroupService, AdmAuditLogService auditLogService) {
        this.transactionGroupService = transactionGroupService;
        this.auditLogService = auditLogService;
    }

    @GetMapping    @Operation(operationId = "admTransactionGroupFindGroups", summary = "거래 그룹 목록", description = "transactionId 기준으로 복합 거래 그룹, 전체 수행시간, 실패 구간, 사용자/운영자/회원/고객 검색 조건을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findGroups(@RequestParam Map<String, String> criteria) {
        return ResponseEntity.ok(transactionGroupService.findGroups(criteria));
    }

    @PostMapping("/subject-search")
    @Operation(operationId = "admTransactionGroupFindBySubject", summary = "Subject 기반 거래 검색",
            description = "보호 Search Token으로 ACTOR Subject를 찾고 transactionId 목록을 반환합니다. Raw Subject는 URL/일반 Audit에 저장하지 않습니다.")
    public ResponseEntity<Map<String, Object>> findBySubject(@RequestBody SubjectSearchRequest body, HttpServletRequest request) {
        String operator = requireOperator(request);
        String reason = auditLogService.requireReason(body.reason());
        Map<String, Object> result = transactionGroupService.findBySubject(body.subjectType(), body.subjectId(), body.from(), body.to(), body.limit());
        Object items = result.get("items");
        int resultCount = items instanceof java.util.Collection<?> collection ? collection.size() : 0;
        auditLogService.record(CpfContexts.transactionId(), operator, "SUBJECT_TIMELINE_SEARCH", "TRANSACTION_SUBJECT",
                String.valueOf(result.getOrDefault("maskedSubject", "masked")), reason, null,
                "{\"resultCount\":" + resultCount + "}", null, clientIp(request));
        return ResponseEntity.ok(result);
    }

    public record SubjectSearchRequest(String subjectType, String subjectId, String from, String to, int limit, String reason) { }

    @GetMapping("/{transactionId}")    @Operation(operationId = "admTransactionGroupFindDetail", summary = "거래 그룹 상세", description = "transactionId 하나로 구간/트리/외부호출/Message/DLQ/Batch/File/Trace/Audit/UNKNOWN-reconcile lineage와 freshness를 함께 조회합니다.")
    public ResponseEntity<Map<String, Object>> findDetail(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionGroupService.findDetail(transactionId));
    }

    @GetMapping("/{transactionId}/segments")    @Operation(operationId = "admTransactionGroupFindSegments", summary = "거래 구간 목록", description = "거래 그룹에 포함된 segment flat 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findSegments(@PathVariable String transactionId) {
        return ResponseEntity.ok(Map.of(
                "transactionId", transactionId,
                "items", transactionGroupService.findSegments(transactionId)));
    }

    @GetMapping("/{transactionId}/timeline")    @Operation(operationId = "admTransactionGroupFindTimeline", summary = "거래 timeline", description = "parentSegmentId와 callDepth를 포함한 timeline 구간 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findTimeline(@PathVariable String transactionId) {
        return ResponseEntity.ok(Map.of(
                "transactionId", transactionId,
                "items", transactionGroupService.findTimeline(transactionId)));
    }

    @GetMapping("/{transactionId}/headers")    @Operation(operationId = "admTransactionGroupFindHeaders", summary = "거래 헤더 snapshot", description = "구간별 마스킹된 요청/응답/확장 헤더 snapshot을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findHeaders(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionGroupService.findHeaders(transactionId));
    }

    @GetMapping("/{transactionId}/external-logs")    @Operation(operationId = "admTransactionGroupFindExternalLogs", summary = "외부연계 송수신 로그", description = "cpf_transaction_segment에 기록된 표준 외부 호출 구간을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findExternalLogs(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionGroupService.findExternalLogs(transactionId));
    }
}
