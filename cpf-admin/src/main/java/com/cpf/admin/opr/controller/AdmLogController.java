package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmLogQueryService;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.cpf.web.api.CpfController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM 거래 로그 관제 API입니다.
 *
 * <p>거래/Domain/Instance 축을 한 화면에서 교차 조회하여 다중 WAS 및 Generated Domain 장애를
 * transactionId 기준으로 추적할 수 있게 합니다.</p>
 */
@CpfController
@RequestMapping("/adm/api/logs")
@Tag(name = "ADM-Logs", description = "CPF 거래 로그 조회와 상세 포맷팅 API")
public class AdmLogController extends com.cpf.admin.common.base.AdmBaseController {
    private static final Logger log = LoggerFactory.getLogger(AdmLogController.class);
    private final AdmLogQueryService logQueryService;

    public AdmLogController(AdmLogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @GetMapping
    @CpfOnlineTransaction(id = "OADMOP0001", name = "ADMTransactionLogList", ownerDomain="ADM")
    @Operation(operationId = "admLogFindLogs",
            summary = "거래 로그 목록 조회",
            description = "transactionId/traceId와 자동 수집된 system/domain/application/instance/starter/capability/provider/operation 메타데이터를 기준으로 모든 Domain의 CPF DB 로그를 통합 검색합니다.")
    public ResponseEntity<Map<String, Object>> findLogs(
            @RequestParam(required = false) String transactionId,

            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String businessTransactionId,
            @RequestParam(required = false) String memberNo,
            @RequestParam(required = false) String customerNo,
            @RequestParam(required = false) String uri,
            @RequestParam(required = false) String responseCode,
            @RequestParam(required = false) Integer httpStatus,
            @RequestParam(required = false) String channelCode,
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String moduleId,
            @RequestParam(required = false) String wasId,
            @RequestParam(required = false) String serverInstanceId,
            @RequestParam(required = false) String hostName,
            @RequestParam(required = false) String systemCode,
            @RequestParam(required = false) String domainCode,
            @RequestParam(required = false) String application,
            @RequestParam(required = false) String starterId,
            @RequestParam(required = false) String capabilityId,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String operation,
            @RequestParam(defaultValue = "50") int limit) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("available", true);
            response.put("items", logQueryService.findLogs(
                    transactionId, traceId, businessTransactionId, memberNo, customerNo,
                    uri, responseCode, httpStatus, channelCode, logType,
                    moduleId, wasId, serverInstanceId, hostName,
                    systemCode, domainCode, application, starterId, capabilityId, provider, operation, limit));
        } catch (DataAccessException ex) {
            log.error("ADM transaction log query failed.", ex);
            response.put("available", false);
            response.put("items", java.util.List.of());
            response.put("message", "CPF 거래 로그 DB를 사용할 수 없습니다. 운영 로그에서 원인을 확인하십시오.");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{logIdx}")
    @CpfOnlineTransaction(id = "OADMOP0002", name = "ADMTransactionLogDetail", ownerDomain="ADM")
    @Operation(operationId = "admLogGetLogDetail",
            summary = "거래 로그 상세 조회",
            description = "거래 요약, 상세 로그, JSON pretty 결과와 고정길이 전문 Raw/Masked 값을 조회합니다. Layout Metadata가 연결된 경우에만 별도 필드 해석을 제공합니다.")
    public ResponseEntity<Map<String, Object>> getLogDetail(@PathVariable Long logIdx) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("available", true);
            response.put("item", logQueryService.getLogDetail(logIdx));
        } catch (DataAccessException ex) {
            log.error("ADM transaction log detail query failed. logIdx={}", logIdx, ex);
            response.put("available", false);
            response.put("item", null);
            response.put("message", "CPF 거래 로그 상세를 사용할 수 없습니다. 운영 로그에서 원인을 확인하십시오.");
        }
        return ResponseEntity.ok(response);
    }

}
