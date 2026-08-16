/* ADM/BZA 실제 Consumer가 CPF Framework Annotation을 사용하도록 currentize한다. */
package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.health.AdmHealthInstanceRegistry;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.platform.operations.api.health.CpfRuntimeHealth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.cpf.web.api.CpfController;
import org.springframework.web.server.ResponseStatusException;

@CpfController
@Tag(name="ADM-Health-Instances", description="Multi-instance health aggregation")
public class AdmInstanceHealthController extends com.cpf.admin.common.base.AdmBaseController {
    private static final String TOKEN_HEADER = "X-Cpf-Runtime-Agent-Token";
    private final AdmHealthInstanceRegistry registry;
    private final String agentToken;

    public AdmInstanceHealthController(AdmHealthInstanceRegistry registry,
            @Value("${cpf.runtime.control.agent-token:${CPF_RUNTIME_CONTROL_AGENT_TOKEN:}}") String agentToken) {
        this.registry = registry; this.agentToken = agentToken == null ? "" : agentToken;
    }

    @PostMapping("/cpf/health/instances/report")
    @Operation(operationId="cpfHealthInstanceReport", summary="Runtime instance health report")
    public ResponseEntity<Void> report(@RequestHeader(TOKEN_HEADER) String token, @RequestBody CpfRuntimeHealth health) {
        authenticateAgent(token); registry.report(health); return ResponseEntity.accepted().build();
    }

    @GetMapping("/adm/api/health/instances")
    @CpfOnlineTransaction(id="OADMHL0010", name="ADMHealthInstanceList", ownerDomain="ADM")
    @Operation(operationId="admHealthInstanceList", summary="Instance health list")
    public ResponseEntity<Map<String,Object>> list(@RequestParam(required=false) String systemId,
            @RequestParam(required=false) String readiness,
            @RequestParam(defaultValue="false") boolean includeStale,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        List<AdmHealthInstanceRegistry.Entry> items = registry.search(systemId, readiness, includeStale, page, size);
        return ResponseEntity.ok(Map.of("items", items, "page", Math.max(0,page), "size", Math.min(200,Math.max(1,size)),
                "total", registry.count(systemId,readiness,includeStale)));
    }

    @GetMapping("/adm/api/health/instances/{systemId}/{instanceId}")
    @CpfOnlineTransaction(id="OADMHL0020", name="ADMHealthInstanceDetail", ownerDomain="ADM")
    @Operation(operationId="admHealthInstanceDetail", summary="Instance health detail")
    public ResponseEntity<AdmHealthInstanceRegistry.Entry> detail(@PathVariable String systemId, @PathVariable String instanceId) {
        return registry.find(systemId,instanceId).map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "health instance not found"));
    }

    private void authenticateAgent(String provided) {
        if (agentToken.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Runtime Agent credential is not configured");
        byte[] expected = agentToken.getBytes(StandardCharsets.UTF_8);
        byte[] actual = (provided == null ? "" : provided).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Runtime Agent authentication failed");
    }
}
