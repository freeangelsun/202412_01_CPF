package com.cpf.bizadmin.audit.controller;

import org.springframework.web.bind.annotation.RestController;
import com.cpf.bizadmin.audit.service.BzaBusinessAuditService;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** BZA 감사 체인 무결성 운영 API. */
@RestController
@RequestMapping("/api/bza/audits")
public class BzaBusinessAuditController extends com.cpf.bizadmin.common.base.BzaBaseController {
  private final BzaBusinessAuditService s;

  public BzaBusinessAuditController(BzaBusinessAuditService s) {
    this.s = s;
  }

  @GetMapping("/verify")  @Operation(operationId = "bzaBusinessAuditVerify", summary = "업무 감사 체인 무결성 검증")
  public ResponseEntity<Map<String, Object>> verify() {
    return ResponseEntity.ok(s.verify());
  }
}
