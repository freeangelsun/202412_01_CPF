package com.cpf.account.account.controller;

import com.cpf.account.account.dto.AccAccountCreateRequest;
import com.cpf.account.account.dto.AccAccountResponse;
import com.cpf.account.account.dto.AccAccountSearchCriteria;
import com.cpf.account.account.dto.AccAccountUpdateRequest;
import com.cpf.account.account.service.AccAccountService;
import com.cpf.account.common.base.AccBaseController;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 생성기 검증용 ACC reference domain의 중립 계정 CRUD API입니다. */
@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "ACC 계정", description = "생성 도메인의 CRUD·검색·동시성·감사 표준을 검증하는 reference API")
public class AccAccountController extends AccBaseController {
    private final AccAccountService service;

    public AccAccountController(AccAccountService service) {
        this.service = service;
    }

    @PostMapping
    @CpfOnlineTransaction(
            id = "OACCAC0001", name = "ACC 계정 등록", ownerDomain = "ACC",
            description = "중립 계정을 등록하고 감사 변경 이력을 남깁니다.",
            requiredPermission = "ACC_ACCOUNT_CREATE", auditReasonRequired = true)
    @Operation(operationId = "createAccAccount", summary = "ACC 계정 등록")
    public ResponseEntity<AccAccountResponse> create(
            @Valid @RequestBody AccAccountCreateRequest request,
            @RequestHeader("X-Operator-Id") String operatorId,
            @RequestHeader("X-Audit-Reason") String auditReason) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, operatorId, auditReason));
    }

    @GetMapping("/{accountId}")
    @CpfOnlineTransaction(
            id = "OACCAC0002", name = "ACC 계정 단건 조회", ownerDomain = "ACC",
            requiredPermission = "ACC_ACCOUNT_READ")
    @Operation(operationId = "getAccAccount", summary = "ACC 계정 단건 조회")
    public ResponseEntity<AccAccountResponse> get(@PathVariable long accountId) {
        return ok(service.get(accountId));
    }

    @GetMapping
    @CpfOnlineTransaction(
            id = "OACCAC0003", name = "ACC 계정 목록 조회", ownerDomain = "ACC",
            requiredPermission = "ACC_ACCOUNT_READ")
    @Operation(operationId = "searchAccAccounts", summary = "ACC 계정 검색", description = "offset 또는 cursor 기반 페이징과 정렬 whitelist를 적용합니다.")
    public ResponseEntity<List<AccAccountResponse>> search(AccAccountSearchCriteria criteria) {
        return ok(service.search(criteria));
    }

    @PutMapping("/{accountId}")
    @CpfOnlineTransaction(
            id = "OACCAC0004", name = "ACC 계정 수정", ownerDomain = "ACC",
            requiredPermission = "ACC_ACCOUNT_UPDATE", auditReasonRequired = true)
    @Operation(operationId = "updateAccAccount", summary = "ACC 계정 수정")
    public ResponseEntity<AccAccountResponse> update(
            @PathVariable long accountId,
            @Valid @RequestBody AccAccountUpdateRequest request,
            @RequestHeader("X-Operator-Id") String operatorId,
            @RequestHeader("X-Audit-Reason") String auditReason) {
        return ok(service.update(accountId, request, operatorId, auditReason));
    }

    @DeleteMapping("/{accountId}")
    @CpfOnlineTransaction(
            id = "OACCAC0005", name = "ACC 계정 논리 삭제", ownerDomain = "ACC",
            requiredPermission = "ACC_ACCOUNT_DELETE", auditReasonRequired = true)
    @Operation(operationId = "deleteAccAccount", summary = "ACC 계정 논리 삭제")
    public ResponseEntity<Void> delete(
            @PathVariable long accountId,
            @RequestParam long version,
            @RequestHeader("X-Operator-Id") String operatorId,
            @RequestHeader("X-Audit-Reason") String auditReason) {
        service.delete(accountId, version, operatorId, auditReason);
        return ResponseEntity.noContent().build();
    }
}
