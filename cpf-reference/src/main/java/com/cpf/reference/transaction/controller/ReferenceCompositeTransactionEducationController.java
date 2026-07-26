package com.cpf.reference.transaction.controller;

import com.cpf.core.common.execution.CpfOnlineTransaction;
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
@RequestMapping({"/api/reference/transactions", "/reference/edu/transactions"})
@Tag(name = "REF Reference Composite Transaction", description = "복합 거래 trace 샘플 안내 API")
public class ReferenceCompositeTransactionEducationController extends com.cpf.reference.common.base.ReferenceBaseController {

    @GetMapping("/composite-sample")
    @CpfOnlineTransaction(id = "OREFAA0043", name = "REFCompositeTransactionSample")
    @Operation(
            operationId = "refCompositeTransactionEducationCompositeSample",
            summary = "복합 거래 추적 샘플 안내",
            description = "REF에서 CPF Service Call Engine으로 중립 시뮬레이터를 호출하고 ADM에서 타임라인을 확인하는 절차를 안내합니다.")
    public ResponseEntity<Map<String, Object>> compositeSample() {
        return ResponseEntity.ok(Map.of(
                "purpose", "transactionId 하나로 여러 모듈의 segment를 묶어 운영자가 timeline으로 조회하는 샘플입니다.",
                "patterns", List.of(
                        "REF Service Call Engine 샘플에서 REF 자체 중립 endpoint를 호출",
                        "동일 X-Transaction-Id를 승계하고 각 호출 구간은 X-Transaction-Segment-Id로 분리"),
                "admApis", List.of(
                        "GET /adm/api/transaction-groups",
                        "GET /adm/api/transaction-groups/{transactionId}",
                        "GET /adm/api/transaction-groups/{transactionId}/timeline"),
                "requiredHeaders", List.of(
                        "X-Transaction-Id",
                        "X-Transaction-Segment-Id",
                        "X-Parent-Transaction-Segment-Id")));
    }
}
