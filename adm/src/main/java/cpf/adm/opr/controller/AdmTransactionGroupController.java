package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmTransactionGroupService;
import cpf.pfw.common.logging.CpfTransaction;
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
@Tag(name = "ADM-TransactionGroup", description = "transactionGlobalId 그룹, segment, timeline 조회 API")
public class AdmTransactionGroupController {
    private final AdmTransactionGroupService transactionGroupService;

    public AdmTransactionGroupController(AdmTransactionGroupService transactionGroupService) {
        this.transactionGroupService = transactionGroupService;
    }

    @GetMapping
    @CpfTransaction(id = "ADM01TRG0001", name = "ADMTransactionGroupList")
    @Operation(summary = "거래 그룹 목록", description = "transactionGlobalId 기준으로 복합 거래 그룹 목록과 전체 수행시간, 실패 구간을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findGroups(@RequestParam Map<String, String> criteria) {
        return ResponseEntity.ok(transactionGroupService.findGroups(criteria));
    }

    @GetMapping("/{transactionGlobalId}")
    @CpfTransaction(id = "ADM01TRG0002", name = "ADMTransactionGroupDetail")
    @Operation(summary = "거래 그룹 상세", description = "transactionGlobalId 기준 segment, timeline, header snapshot, 외부연계 후보 로그를 함께 조회합니다.")
    public ResponseEntity<Map<String, Object>> findDetail(@PathVariable String transactionGlobalId) {
        return ResponseEntity.ok(transactionGroupService.findDetail(transactionGlobalId));
    }

    @GetMapping("/{transactionGlobalId}/segments")
    @CpfTransaction(id = "ADM01TRG0003", name = "ADMTransactionGroupSegments")
    @Operation(summary = "거래 구간 목록", description = "거래 그룹의 segment flat 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findSegments(@PathVariable String transactionGlobalId) {
        return ResponseEntity.ok(Map.of(
                "transactionGlobalId", transactionGlobalId,
                "items", transactionGroupService.findSegments(transactionGlobalId)));
    }

    @GetMapping("/{transactionGlobalId}/timeline")
    @CpfTransaction(id = "ADM01TRG0004", name = "ADMTransactionGroupTimeline")
    @Operation(summary = "거래 timeline", description = "parentSegmentId와 callDepth를 포함한 timeline 구간 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findTimeline(@PathVariable String transactionGlobalId) {
        return ResponseEntity.ok(Map.of(
                "transactionGlobalId", transactionGlobalId,
                "items", transactionGroupService.findTimeline(transactionGlobalId)));
    }

    @GetMapping("/{transactionGlobalId}/headers")
    @CpfTransaction(id = "ADM01TRG0005", name = "ADMTransactionGroupHeaders")
    @Operation(summary = "거래 헤더 snapshot", description = "구간별 마스킹된 요청/응답/확장 헤더 snapshot을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findHeaders(@PathVariable String transactionGlobalId) {
        return ResponseEntity.ok(transactionGroupService.findHeaders(transactionGlobalId));
    }

    @GetMapping("/{transactionGlobalId}/external-logs")
    @CpfTransaction(id = "ADM01TRG0006", name = "ADMTransactionGroupExternalLogs")
    @Operation(summary = "외부연계 후보 로그", description = "EXTERNAL role 또는 외부기관 코드가 있는 구간을 외부연계 후보 로그로 조회합니다.")
    public ResponseEntity<Map<String, Object>> findExternalLogs(@PathVariable String transactionGlobalId) {
        return ResponseEntity.ok(transactionGroupService.findExternalLogs(transactionGlobalId));
    }
}
