package com.cpf.reference.catalog.controller;

import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/reference", "/reference/edu"})
@Tag(name = "REF Reference 00. Catalog", description = "REF 교육 API 카탈로그")
public class ReferenceEducationController extends com.cpf.reference.common.base.ReferenceBaseController {

    @GetMapping
    @CpfOnlineTransaction(id = "OREFAA0099", name = "REFEducationCatalog")
    @Operation(operationId = "refEducationCatalog", summary = "REF 교육 카탈로그", description = "개발자가 상황별로 참고할 수 있는 EDU 샘플 API 그룹을 조회합니다.")
    public ResponseEntity<Map<String, Object>> catalog() {
        return ResponseEntity.ok(Map.of(
                "purpose", "CPF 프레임워크 표준 기능을 학습하기 위한 교육 API입니다.",
                "groups", List.of(
                        Map.of("name", "CRUD", "apis", List.of(
                                "GET /api/reference/items",
                                "GET /api/reference/items/{educationItemId}",
                                "POST /api/reference/items",
                                "PUT /api/reference/items/{educationItemId}",
                                "PATCH /api/reference/items/{educationItemId}/status",
                                "DELETE /api/reference/items/{educationItemId}",
                                "GET /api/reference/crud-items")),
                        Map.of("name", "Query", "apis", List.of(
                                "GET /api/reference/query/items",
                                "GET /api/reference/query/items/page",
                                "GET /api/reference/query/items/keyset",
                                "GET /api/reference/query/headers")),
                        Map.of("name", "CMN cache", "apis", List.of("GET /api/reference/cache", "POST /api/reference/cache/refresh", "POST /api/reference/cmn/code")),
                        Map.of("name", "Exception", "apis", List.of("GET /api/reference/exception", "GET /api/reference/exception/dynamic-message")),
                        Map.of("name", "Utility", "apis", List.of("GET /api/reference/utils", "GET /api/reference/headers")),
                        Map.of("name", "Messaging", "apis", List.of("POST /api/reference/messaging/publish", "GET /api/reference/messaging/recent")),
                        Map.of("name", "Fixed length", "apis", List.of("POST /api/reference/fixed-length/marshal", "POST /api/reference/fixed-length/unmarshal")),
                        Map.of("name", "Service call", "apis", List.of("GET /api/reference/service-call/mbr-detail", "GET /api/reference/webclient/external-get")),
                        Map.of("name", "File exchange", "apis", List.of("POST /api/reference/file-exchange/local/write", "POST /api/reference/file-exchange/transfer-plan")),
                        Map.of("name", "Transaction", "apis", List.of(
                                "POST /api/reference/transaction/single",
                                "POST /api/reference/transaction/separated",
                                "GET /api/reference/transactions/composite-sample")),
                        Map.of("name", "Dynamic log", "apis", List.of("PUT /api/reference/admin/log-level", "GET /api/reference/admin/log-level")),
                        Map.of("name", "Security", "apis", List.of("GET /api/reference/security/crypto/basic", "POST /api/reference/security/jwt/create", "GET /api/reference/security/oauth/introspect")),
                        Map.of("name", "CMN sample", "apis", List.of(
                                "GET /api/reference/cmn-sample/status",
                                "GET /api/reference/cmn-sample/items",
                                "GET /api/reference/cmn-sample/items/cursor",
                                "POST /api/reference/cmn-sample/items",
                                "PUT /api/reference/cmn-sample/items/{sampleItemId}",
                                "DELETE /api/reference/cmn-sample/items/{sampleItemId}",
                                "POST /api/reference/cmn-sample/transaction/rollback-verify")),
                        Map.of("name", "Batch", "apis", List.of(
                                "POST /api/reference/batch/tasklet/run",
                                "POST /api/reference/batch/chunk/run",
                                "POST /api/reference/batch/retry/run",
                                "GET /api/reference/batch/retry-policy",
                                "GET /api/reference/batch/lock-policy",
                                "GET /api/reference/batch/checkpoint-restart",
                                "GET /api/reference/batch/adm-link",
                                "GET /api/reference/batch/schedule-policy")))));
    }
}
