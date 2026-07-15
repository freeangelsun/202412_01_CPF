package cpf.xyz.edu.controller;

import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 복합 거래 trace 샘플을 개발자에게 안내하는 EDU API입니다.
 */
@RestController
@RequestMapping("/xyz/edu/transactions")
@Tag(name = "XYZ-EDU Composite Transaction", description = "복합 거래 trace 샘플 안내 API")
public class XyzCompositeTransactionEducationController {

    @GetMapping("/composite-sample")
    @CpfOnlineTransaction(id = "OXYZ-EDU-09-0040", name = "XYZCompositeTransactionSample")
    @Operation(operationId = "xyzCompositeTransactionEducationCompositeSample", summary = "복합 거래 추적 샘플 안내", description = "XYZ에서 PFW Service Call Engine으로 MBR을 호출하고 ADM에서 타임라인을 확인하는 절차를 안내합니다.")
    public ResponseEntity<Map<String, Object>> compositeSample() {
        return ResponseEntity.ok(Map.of(
                "purpose", "transactionGlobalId 기준으로 여러 모듈의 segment를 묶어 운영자가 timeline으로 조회하는 샘플입니다.",
                "patterns", List.of(
                        "XYZ Service Call Engine 샘플에서 MBR_MEMBER_SUMMARY endpoint를 호출",
                        "동일 X-Transaction-Id를 전달해 호출 구간을 하나의 타임라인으로 연결"),
                "admApis", List.of(
                        "GET /adm/api/transaction-groups",
                        "GET /adm/api/transaction-groups/{transactionGlobalId}",
                        "GET /adm/api/transaction-groups/{transactionGlobalId}/timeline"),
                "requiredHeaders", List.of(
                        "X-Transaction-Id",
                        "X-Transaction-Segment-Id",
                        "X-Parent-Transaction-Segment-Id",
                        "X-Root-Transaction-Id")));
    }
}
