package com.cpf.education.operations.centercut.controller;
import com.cpf.batch.api.centercut.CpfCenterCutResult;
import com.cpf.batch.api.centercut.CpfCenterCutTarget;
import com.cpf.foundation.execution.api.CpfSharedApi;
import com.cpf.education.operations.centercut.EducationCenterCutHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BAT Center-Cut Runtime이 호출하는 EDU 업무 item adapter EDU입니다.
 * Runner/재시도/lease/UNKNOWN_RESULT 판정은 BAT가 소유하며 EDU는 업무 처리만 담당합니다.
 */
@RestController
@RequestMapping({"/api/education/center-cut", "/education/edu/center-cut", "/internal/ref/center-cut"})
@Tag(name = "EDU Education 14. Center-Cut", description = "BAT Runtime과 업무 Domain의 Center-Cut Handler/SPI 경계 예제")
public class EducationCenterCutEducationController extends com.cpf.education.base.EducationBaseController {
    private final EducationCenterCutHandler handler;

    public EducationCenterCutEducationController(EducationCenterCutHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/items")
    @CpfSharedApi(
            id = "SEDUCC0001",
            name = "EDUCenterCutItem처리",
            ownerDomain = "EDU",
            description = "BAT가 Claim한 단일 Education Center-Cut item을 처리합니다.",
            allowedCallers = "BAT")
    @Operation(operationId = "refCenterCutItemHandle",
            summary = "BAT가 선택한 Center-Cut item 업무 처리",
            description = "BAT가 transactionId/segment 계층과 재처리 정책을 관리하고 EDU는 단일 업무 item만 처리합니다.")
    /** handle 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<CpfCenterCutResult> handle(@RequestBody CpfCenterCutTarget target) {
        return ResponseEntity.ok(handler.handle(target));
    }
}
