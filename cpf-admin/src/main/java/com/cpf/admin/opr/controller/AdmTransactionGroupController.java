package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmTransactionGroupService;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * transactionGlobalId 기준 복합 거래 그룹 조회 API입니다.
 */
@RestController
@RequestMapping("/adm/api/transaction-groups")
@Tag(name = "ADM-TransactionGroup", description = "transactionGlobalId 그룹, 구간, 타임라인, 헤더, 외부 호출 조회 API")
public class AdmTransactionGroupController extends cpf.adm.common.base.AdmBaseController {
    private final AdmTransactionGroupService transactionGroupService;

    public AdmTransactionGroupController(AdmTransactionGroupService transactionGroupService) {
        this.transactionGroupService = transactionGroupService;
    }

    @GetMapping
    @CpfOnlineTransaction(id = "OADMTR0001", name = "ADMTransactionGroupList")
    @Operation(operationId = "admTransactionGroupFindGroups", summary = "거래 그룹 목록", description = "transactionGlobalId 기준으로 복합 거래 그룹, 전체 수행시간, 실패 구간, 사용자/운영자/회원/고객 검색 조건을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findGroups(@RequestParam Map<String, String> criteria) {
        return ResponseEntity.ok(transactionGroupService.findGroups(criteria));
    }

    @GetMapping("/{transactionGlobalId}")
    @CpfOnlineTransaction(id = "OADMTR0002", name = "ADMTransactionGroupDetail")
    @Operation(operationId = "admTransactionGroupFindDetail", summary = "거래 그룹 상세", description = "transactionGlobalId 기준 구간, 타임라인, 헤더 스냅샷, 표준 외부 호출 로그를 함께 조회합니다.")
    public ResponseEntity<Map<String, Object>> findDetail(@PathVariable String transactionGlobalId) {
        return ResponseEntity.ok(transactionGroupService.findDetail(transactionGlobalId));
    }

    @GetMapping("/{transactionGlobalId}/segments")
    @CpfOnlineTransaction(id = "OADMTR0003", name = "ADMTransactionGroupSegments")
    @Operation(operationId = "admTransactionGroupFindSegments", summary = "거래 구간 목록", description = "거래 그룹에 포함된 segment flat 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findSegments(@PathVariable String transactionGlobalId) {
        return ResponseEntity.ok(Map.of(
                "transactionGlobalId", transactionGlobalId,
                "items", transactionGroupService.findSegments(transactionGlobalId)));
    }

    @GetMapping("/{transactionGlobalId}/timeline")
    @CpfOnlineTransaction(id = "OADMTR0004", name = "ADMTransactionGroupTimeline")
    @Operation(operationId = "admTransactionGroupFindTimeline", summary = "거래 timeline", description = "parentSegmentId와 callDepth를 포함한 timeline 구간 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findTimeline(@PathVariable String transactionGlobalId) {
        return ResponseEntity.ok(Map.of(
                "transactionGlobalId", transactionGlobalId,
                "items", transactionGroupService.findTimeline(transactionGlobalId)));
    }

    @GetMapping("/{transactionGlobalId}/headers")
    @CpfOnlineTransaction(id = "OADMTR0005", name = "ADMTransactionGroupHeaders")
    @Operation(operationId = "admTransactionGroupFindHeaders", summary = "거래 헤더 snapshot", description = "구간별 마스킹된 요청/응답/확장 헤더 snapshot을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findHeaders(@PathVariable String transactionGlobalId) {
        return ResponseEntity.ok(transactionGroupService.findHeaders(transactionGlobalId));
    }

    @GetMapping("/{transactionGlobalId}/external-logs")
    @CpfOnlineTransaction(id = "OADMTR0006", name = "ADMTransactionGroupExternalLogs")
    @Operation(operationId = "admTransactionGroupFindExternalLogs", summary = "외부연계 송수신 로그", description = "pfw_transaction_segment에 기록된 표준 외부 호출 구간을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findExternalLogs(@PathVariable String transactionGlobalId) {
        return ResponseEntity.ok(transactionGroupService.findExternalLogs(transactionGlobalId));
    }
}
