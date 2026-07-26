package com.cpf.reference.centercut.controller;

import com.cpf.core.api.centercut.CpfCenterCutResult;
import com.cpf.core.api.centercut.CpfCenterCutTarget;
import com.cpf.core.api.execution.CpfSharedApi;
import com.cpf.reference.centercut.ReferenceCenterCutHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BAT Center-Cut Runtime이 호출하는 REF 업무 item adapter EDU입니다.
 * Runner/재시도/lease/UNKNOWN_RESULT 판정은 BAT가 소유하며 REF는 업무 처리만 담당합니다.
 */
@RestController
@RequestMapping({"/api/reference/center-cut", "/reference/edu/center-cut", "/internal/ref/center-cut"})
@Tag(name = "REF Reference 14. Center-Cut", description = "BAT Runtime과 업무 Domain의 Center-Cut Handler/SPI 경계 예제")
public class ReferenceCenterCutEducationController extends com.cpf.reference.common.base.ReferenceBaseController {
    private final ReferenceCenterCutHandler handler;

    public ReferenceCenterCutEducationController(ReferenceCenterCutHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/items")
    @CpfSharedApi(
            id = "SREFCC0001",
            name = "REFCenterCutItem처리",
            ownerDomain = "REF",
            description = "BAT가 Claim한 단일 Reference Center-Cut item을 처리합니다.",
            allowedCallers = "BAT")
    @Operation(operationId = "refCenterCutItemHandle",
            summary = "BAT가 선택한 Center-Cut item 업무 처리",
            description = "BAT가 transactionId/segment 계층과 재처리 정책을 관리하고 REF는 단일 업무 item만 처리합니다.")
    public ResponseEntity<CpfCenterCutResult> handle(@RequestBody CpfCenterCutTarget target) {
        return ResponseEntity.ok(handler.handle(target));
    }
}
