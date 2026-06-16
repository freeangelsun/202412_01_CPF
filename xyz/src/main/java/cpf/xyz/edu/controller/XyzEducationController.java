package cpf.xyz.edu.controller;

import cpf.pfw.common.logging.FpsTransaction;
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
@Tag(name = "XYZ-EDU 00. Catalog", description = "XYZ education API catalog")
public class XyzEducationController {

    @GetMapping
    @FpsTransaction(id = "XYZ01EDU0099", name = "XYZEducationCatalog")
    @Operation(summary = "XYZ education catalog", description = "Lists the education sample API groups.")
    public ResponseEntity<Map<String, Object>> catalog() {
        return ResponseEntity.ok(Map.of(
                "purpose", "Education APIs for CPF framework samples.",
                "groups", List.of(
                        Map.of("name", "CRUD", "apis", List.of("GET /xyz/edu/samples", "GET /xyz/edu/samples/detail", "POST /xyz/edu/samples")),
                        Map.of("name", "CMN cache", "apis", List.of("GET /xyz/edu/cache", "POST /xyz/edu/cache/refresh", "POST /xyz/edu/cmn/code")),
                        Map.of("name", "Exception", "apis", List.of("GET /xyz/edu/exception", "GET /xyz/edu/exception/dynamic-message")),
                        Map.of("name", "Utility", "apis", List.of("GET /xyz/edu/utils", "GET /xyz/edu/headers")),
                        Map.of("name", "Messaging", "apis", List.of("POST /xyz/edu/messaging/publish", "GET /xyz/edu/messaging/recent")),
                        Map.of("name", "Fixed length", "apis", List.of("POST /xyz/edu/fixed-length/marshal", "POST /xyz/edu/fixed-length/unmarshal")),
                        Map.of("name", "Service call", "apis", List.of("GET /xyz/edu/service-call/mbr-detail", "GET /xyz/edu/webclient/external-get")),
                        Map.of("name", "File exchange", "apis", List.of("POST /xyz/edu/file-exchange/local/write", "POST /xyz/edu/file-exchange/transfer-plan")),
                        Map.of("name", "Transaction", "apis", List.of("POST /xyz/edu/transaction/single", "POST /xyz/edu/transaction/separated")),
                        Map.of("name", "Dynamic log", "apis", List.of("PUT /xyz/edu/admin/log-level", "GET /xyz/edu/admin/log-level")),
                        Map.of("name", "Security", "apis", List.of("GET /xyz/edu/security/crypto/basic", "POST /xyz/edu/security/jwt/create", "GET /xyz/edu/security/oauth/introspect")))));
    }
}
