package cpf.xyz.edu.catalog.controller;

import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 00. Catalog", description = "XYZ 교육 API 카탈로그")
public class XyzEducationController {

    @GetMapping
    @CpfOnlineTransaction(id = "OXYZAA0099", name = "XYZEducationCatalog")
    @Operation(operationId = "xyzEducationCatalog", summary = "XYZ 교육 카탈로그", description = "개발자가 상황별로 참고할 수 있는 EDU 샘플 API 그룹을 조회합니다.")
    public ResponseEntity<Map<String, Object>> catalog() {
        return ResponseEntity.ok(Map.of(
                "purpose", "CPF 프레임워크 표준 기능을 학습하기 위한 교육 API입니다.",
                "groups", List.of(
                        Map.of("name", "CRUD", "apis", List.of(
                                "GET /xyz/edu/items",
                                "GET /xyz/edu/items/{educationItemId}",
                                "POST /xyz/edu/items",
                                "PUT /xyz/edu/items/{educationItemId}",
                                "PATCH /xyz/edu/items/{educationItemId}/status",
                                "DELETE /xyz/edu/items/{educationItemId}",
                                "GET /xyz/edu/crud-items")),
                        Map.of("name", "Query", "apis", List.of(
                                "GET /xyz/edu/query/items",
                                "GET /xyz/edu/query/items/page",
                                "GET /xyz/edu/query/items/keyset",
                                "GET /xyz/edu/query/headers")),
                        Map.of("name", "CMN cache", "apis", List.of("GET /xyz/edu/cache", "POST /xyz/edu/cache/refresh", "POST /xyz/edu/cmn/code")),
                        Map.of("name", "Exception", "apis", List.of("GET /xyz/edu/exception", "GET /xyz/edu/exception/dynamic-message")),
                        Map.of("name", "Utility", "apis", List.of("GET /xyz/edu/utils", "GET /xyz/edu/headers")),
                        Map.of("name", "Messaging", "apis", List.of("POST /xyz/edu/messaging/publish", "GET /xyz/edu/messaging/recent")),
                        Map.of("name", "Fixed length", "apis", List.of("POST /xyz/edu/fixed-length/marshal", "POST /xyz/edu/fixed-length/unmarshal")),
                        Map.of("name", "Service call", "apis", List.of("GET /xyz/edu/service-call/mbr-detail", "GET /xyz/edu/webclient/external-get")),
                        Map.of("name", "File exchange", "apis", List.of("POST /xyz/edu/file-exchange/local/write", "POST /xyz/edu/file-exchange/transfer-plan")),
                        Map.of("name", "Transaction", "apis", List.of("POST /xyz/edu/transaction/single", "POST /xyz/edu/transaction/separated", "GET /xyz/edu/transactions/composite-sample")),
                        Map.of("name", "Dynamic log", "apis", List.of("PUT /xyz/edu/admin/log-level", "GET /xyz/edu/admin/log-level")),
                        Map.of("name", "Security", "apis", List.of("GET /xyz/edu/security/crypto/basic", "POST /xyz/edu/security/jwt/create", "GET /xyz/edu/security/oauth/introspect")),
                        Map.of("name", "CMN business", "apis", List.of("POST /xyz/edu/cmn-business/sequence/issue", "POST /xyz/edu/cmn-business/notification-log", "POST /xyz/edu/cmn-business/business-log")),
                        Map.of("name", "Batch", "apis", List.of(
                                "POST /xyz/edu/batch/tasklet/run",
                                "POST /xyz/edu/batch/chunk/run",
                                "POST /xyz/edu/batch/retry/run",
                                "GET /xyz/edu/batch/retry-policy",
                                "GET /xyz/edu/batch/lock-policy",
                                "GET /xyz/edu/batch/checkpoint-restart",
                                "GET /xyz/edu/batch/adm-link",
                                "GET /xyz/edu/batch/schedule-policy")))));
    }
}
