package com.cpf.education.verification.catalog.controller;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * EDU가 제공하는 중립 교육 API를 기능 영역별로 안내하는 카탈로그입니다.
 */
@RestController
@RequestMapping({"/api/education", "/education/edu"})
@Tag(name = "EDU Education 00. Catalog", description = "EDU 교육 API 카탈로그")
public class EducationEducationController extends com.cpf.education.base.EducationBaseController {

    @GetMapping
    @CpfOnlineTransaction(id = "OEDUAA0099", name = "EDUEducationCatalog", ownerDomain="EDU")
    @Operation(operationId = "refEducationCatalog", summary = "EDU 교육 카탈로그", description = "개발자가 상황별로 참고할 수 있는 EDU 샘플 API 그룹을 조회합니다.")
    /** catalog 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> catalog() {
        return ResponseEntity.ok(Map.of(
                "purpose", "CPF 프레임워크 표준 기능을 학습하기 위한 교육 API입니다.",
                "groups", List.of(
                        Map.of("name", "CRUD", "apis", List.of(
                                "GET /api/education/items",
                                "GET /api/education/items/{educationItemId}",
                                "POST /api/education/items",
                                "PUT /api/education/items/{educationItemId}",
                                "PATCH /api/education/items/{educationItemId}/status",
                                "DELETE /api/education/items/{educationItemId}",
                                "GET /api/education/crud-items")),
                        Map.of("name", "Query", "apis", List.of(
                                "GET /api/education/query/items",
                                "GET /api/education/query/items/page",
                                "GET /api/education/query/items/keyset",
                                "GET /api/education/query/headers")),
                        Map.of("name", "CMN cache", "apis", List.of("GET /api/education/cache", "POST /api/education/cache/refresh", "POST /api/education/cmn/code")),
                        Map.of("name", "Exception", "apis", List.of("GET /api/education/exception", "GET /api/education/exception/dynamic-message")),
                        Map.of("name", "Utility", "apis", List.of("GET /api/education/utils", "GET /api/education/headers")),
                        Map.of("name", "Messaging", "apis", List.of("POST /api/education/messaging/publish", "GET /api/education/messaging/recent")),
                        Map.of("name", "Fixed length", "apis", List.of("POST /api/education/fixed-length/marshal", "POST /api/education/fixed-length/unmarshal")),
                        Map.of("name", "Service call", "apis", List.of(
                                "GET /api/education/service-call/self-echo",
                                "GET /api/education/service-call/external-simulator")),
                        Map.of("name", "File exchange", "apis", List.of("POST /api/education/file-exchange/local/write", "POST /api/education/file-exchange/transfer-plan")),
                        Map.of("name", "Transaction", "apis", List.of(
                                "POST /api/education/transaction/single",
                                "POST /api/education/transaction/separated",
                                "GET /api/education/transactions/composite-sample")),
                        Map.of("name", "Dynamic log", "apis", List.of("PUT /api/education/admin/log-level", "GET /api/education/admin/log-level")),
                        Map.of("name", "Security", "apis", List.of("GET /api/education/security/crypto/basic", "POST /api/education/security/jwt/create", "GET /api/education/security/oauth/introspect")),
                        Map.of("name", "CMN sample", "apis", List.of(
                                "GET /api/education/cmn-sample/status",
                                "GET /api/education/cmn-sample/items",
                                "GET /api/education/cmn-sample/items/cursor",
                                "POST /api/education/cmn-sample/items",
                                "PUT /api/education/cmn-sample/items/{sampleItemId}",
                                "DELETE /api/education/cmn-sample/items/{sampleItemId}",
                                // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
                                "POST /api/education/cmn-sample/transaction/rollback-verify")),
                        Map.of("name", "Batch", "apis", List.of(
                                "POST /api/education/batch/tasklet/run",
                                "POST /api/education/batch/chunk/run",
                                "POST /api/education/batch/retry/run",
                                "GET /api/education/batch/retry-policy",
                                "GET /api/education/batch/lock-policy",
                                "GET /api/education/batch/checkpoint-restart",
                                "GET /api/education/batch/adm-link",
                                "GET /api/education/batch/ownership",
                                "GET /api/education/batch/schedule-policy",
                                "GET /api/education/batch/lifecycle-policy")))));
    }
}
