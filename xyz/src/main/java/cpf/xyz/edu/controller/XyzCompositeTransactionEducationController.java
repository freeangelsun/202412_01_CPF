package cpf.xyz.edu.controller;

import cpf.pfw.common.logging.CpfTransaction;
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
    @CpfTransaction(id = "XYZ09EDU0001", name = "XYZCompositeTransactionSample")
    @Operation(operationId = "xyzCompositeTransactionEducationCompositeSample", summary = "복합 거래 trace 샘플 안내", description = "ACC/MBR/EXS 복합 거래 샘플 API와 ADM 조회 API를 개발자용으로 안내합니다.")
    public ResponseEntity<Map<String, Object>> compositeSample() {
        return ResponseEntity.ok(Map.of(
                "purpose", "transactionGlobalId 기준으로 여러 모듈의 segment를 묶어 운영자가 timeline으로 조회하는 샘플입니다.",
                "patterns", List.of(
                        "POST /acc/edu/composite/member-then-external?memberId=1",
                        "POST /acc/edu/composite/member-calls-external?memberId=1"),
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
