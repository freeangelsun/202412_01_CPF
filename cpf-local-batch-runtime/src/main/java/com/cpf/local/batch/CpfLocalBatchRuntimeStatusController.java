package com.cpf.local.batch;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Local Batch Launcher의 역할·Port·안전 상태를 조회합니다. */
@RestController
public class CpfLocalBatchRuntimeStatusController {
    private final Environment environment;

    public CpfLocalBatchRuntimeStatusController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/cpf/local/batch/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> roles = new LinkedHashMap<>();
        role(roles, "control-server", true, 8090);
        role(roles, "scheduler", true, 8091);
        role(roles, "worker", true, 8092);
        role(roles, "center-cut", false, 8093);
        role(roles, "host-agent", false, 8094);
        return ResponseEntity.ok(Map.of(
                "runtime", "CPF_LOCAL_BATCH",
                "developmentOnly", true,
                "singleJvm", true,
                "roleContexts", roles,
                "timestamp", Instant.now().toString()));
    }

    private void role(Map<String, Object> roles, String name, boolean defaultEnabled, int defaultPort) {
        roles.put(name, Map.of(
                "enabled", environment.getProperty(
                        "cpf.local.batch.modules." + name, Boolean.class, defaultEnabled),
                "port", environment.getProperty(
                        "cpf.local.batch.ports." + name, Integer.class, defaultPort)));
    }
}
