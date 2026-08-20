package com.cpf.backoffice.online.audit.controller;

import com.cpf.web.api.CpfRestController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;

import com.cpf.backoffice.online.audit.service.BackofficeBusinessAuditService;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** MBW 감사 체인 무결성 운영 API. */
@CpfRestController
@RequestMapping("/api/v1/backoffice/audits")
public class BackofficeBusinessAuditController extends com.cpf.backoffice.online.base.BackofficeBaseController {
  private final BackofficeBusinessAuditService s;

  public BackofficeBusinessAuditController(BackofficeBusinessAuditService s) {
    this.s = s;
  }

  @GetMapping("/verify")  @Operation(operationId = "MBW_BUSINESS_AUDIT_VERIFY", summary = "업무 감사 체인 무결성 검증")
    @CpfOnlineTransaction(operationId = "MBW_BUSINESS_AUDIT_VERIFY", name = "업무 감사 체인 무결성 검증", description = "업무 감사 체인 무결성 검증 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<Map<String, Object>> verify() {
    return ResponseEntity.ok(s.verify());
  }
}
