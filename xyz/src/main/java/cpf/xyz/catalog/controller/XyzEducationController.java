package cpf.xyz.catalog.controller;

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
@RequestMapping({"/api/xyz/reference", "/xyz/edu"})
@Tag(name = "XYZ Reference 00. Catalog", description = "XYZ 교육 API 카탈로그")
public class XyzEducationController extends cpf.xyz.common.base.XyzBaseController {

    @GetMapping
    @CpfOnlineTransaction(id = "OXYZAA0099", name = "XYZEducationCatalog")
    @Operation(operationId = "xyzEducationCatalog", summary = "XYZ 교육 카탈로그", description = "개발자가 상황별로 참고할 수 있는 EDU 샘플 API 그룹을 조회합니다.")
    public ResponseEntity<Map<String, Object>> catalog() {
        return ResponseEntity.ok(Map.of(
                "purpose", "CPF 프레임워크 표준 기능을 학습하기 위한 교육 API입니다.",
                "groups", List.of(
                        Map.of("name", "CRUD", "apis", List.of(
                                "GET /api/xyz/reference/items",
                                "GET /api/xyz/reference/items/{educationItemId}",
                                "POST /api/xyz/reference/items",
                                "PUT /api/xyz/reference/items/{educationItemId}",
                                "PATCH /api/xyz/reference/items/{educationItemId}/status",
                                "DELETE /api/xyz/reference/items/{educationItemId}",
                                "GET /api/xyz/reference/crud-items")),
                        Map.of("name", "Query", "apis", List.of(
                                "GET /api/xyz/reference/query/items",
                                "GET /api/xyz/reference/query/items/page",
                                "GET /api/xyz/reference/query/items/keyset",
                                "GET /api/xyz/reference/query/headers")),
                        Map.of("name", "CMN cache", "apis", List.of("GET /api/xyz/reference/cache", "POST /api/xyz/reference/cache/refresh", "POST /api/xyz/reference/cmn/code")),
                        Map.of("name", "Exception", "apis", List.of("GET /api/xyz/reference/exception", "GET /api/xyz/reference/exception/dynamic-message")),
                        Map.of("name", "Utility", "apis", List.of("GET /api/xyz/reference/utils", "GET /api/xyz/reference/headers")),
                        Map.of("name", "Messaging", "apis", List.of("POST /api/xyz/reference/messaging/publish", "GET /api/xyz/reference/messaging/recent")),
                        Map.of("name", "Fixed length", "apis", List.of("POST /api/xyz/reference/fixed-length/marshal", "POST /api/xyz/reference/fixed-length/unmarshal")),
                        Map.of("name", "Service call", "apis", List.of("GET /api/xyz/reference/service-call/mbr-detail", "GET /api/xyz/reference/webclient/external-get")),
                        Map.of("name", "File exchange", "apis", List.of("POST /api/xyz/reference/file-exchange/local/write", "POST /api/xyz/reference/file-exchange/transfer-plan")),
                        Map.of("name", "Transaction", "apis", List.of(
                                "POST /api/xyz/reference/transaction/single",
                                "POST /api/xyz/reference/transaction/separated",
                                "GET /api/xyz/reference/transactions/composite-sample")),
                        Map.of("name", "Dynamic log", "apis", List.of("PUT /api/xyz/reference/admin/log-level", "GET /api/xyz/reference/admin/log-level")),
                        Map.of("name", "Security", "apis", List.of("GET /api/xyz/reference/security/crypto/basic", "POST /api/xyz/reference/security/jwt/create", "GET /api/xyz/reference/security/oauth/introspect")),
                        Map.of("name", "CMN business", "apis", List.of(
                                "POST /api/xyz/reference/cmn-business/sequence/issue",
                                "POST /api/xyz/reference/cmn-business/notification-log",
                                "POST /api/xyz/reference/cmn-business/business-log")),
                        Map.of("name", "Batch", "apis", List.of(
                                "POST /api/xyz/reference/batch/tasklet/run",
                                "POST /api/xyz/reference/batch/chunk/run",
                                "POST /api/xyz/reference/batch/retry/run",
                                "GET /api/xyz/reference/batch/retry-policy",
                                "GET /api/xyz/reference/batch/lock-policy",
                                "GET /api/xyz/reference/batch/checkpoint-restart",
                                "GET /api/xyz/reference/batch/adm-link",
                                "GET /api/xyz/reference/batch/schedule-policy")))));
    }
}
