package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmLogQueryService;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM 거래 로그 관제 API입니다.
 *
 * <p>운영 화면에서 거래 ID를 transactionId 또는 transactionGlobalId라고 부를 수 있으므로
 * 두 파라미터를 같은 검색 조건으로 처리합니다.</p>
 */
@RestController
@RequestMapping("/adm/api/logs")
@Tag(name = "ADM-Logs", description = "CPF 거래 로그 조회와 상세 포맷팅 API")
public class AdmLogController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmLogQueryService logQueryService;

    public AdmLogController(AdmLogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @GetMapping
    @CpfOnlineTransaction(id = "OADMOP0001", name = "ADMTransactionLogList")
    @Operation(operationId = "admLogFindLogs",
            summary = "거래 로그 목록 조회",
            description = "transactionId 또는 transactionGlobalId, traceId, 업무 거래 ID, URI, 응답코드, HTTP 상태, 회원번호, 고객번호 기준으로 거래 로그를 검색합니다.")
    public ResponseEntity<Map<String, Object>> findLogs(
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String transactionGlobalId,
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String businessTransactionId,
            @RequestParam(required = false) String memberNo,
            @RequestParam(required = false) String customerNo,
            @RequestParam(required = false) String uri,
            @RequestParam(required = false) String responseCode,
            @RequestParam(required = false) Integer httpStatus,
            @RequestParam(required = false) String channelCode,
            @RequestParam(required = false) String logType,
            @RequestParam(defaultValue = "50") int limit) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("available", true);
            response.put("items", logQueryService.findLogs(
                    firstText(transactionId, transactionGlobalId), traceId, businessTransactionId, memberNo, customerNo,
                    uri, responseCode, httpStatus, channelCode, logType, limit));
        } catch (DataAccessException ex) {
            response.put("available", false);
            response.put("items", java.util.List.of());
            response.put("message", "CPF 거래 로그 DB를 사용할 수 없습니다.");
            response.put("detail", ex.getMostSpecificCause().getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{logIdx}")
    @CpfOnlineTransaction(id = "OADMOP0002", name = "ADMTransactionLogDetail")
    @Operation(operationId = "admLogGetLogDetail",
            summary = "거래 로그 상세 조회",
            description = "거래 요약, 상세 로그, JSON pretty 결과, 고정길이 전문 필드 분해 결과를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getLogDetail(@PathVariable Long logIdx) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("available", true);
            response.put("item", logQueryService.getLogDetail(logIdx));
        } catch (DataAccessException ex) {
            response.put("available", false);
            response.put("item", null);
            response.put("message", "CPF 거래 로그 상세를 사용할 수 없습니다.");
            response.put("detail", ex.getMostSpecificCause().getMessage());
        }
        return ResponseEntity.ok(response);
    }

    private String firstText(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return null;
    }
}
