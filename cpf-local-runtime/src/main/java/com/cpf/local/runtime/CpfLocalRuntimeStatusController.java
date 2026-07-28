package com.cpf.local.runtime;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** 개발 통합 Runtime의 활성 모듈과 안전 상태를 조회합니다. */
@RestController
public class CpfLocalRuntimeStatusController {
    private final Environment environment;

    public CpfLocalRuntimeStatusController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/cpf/local/runtime/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> modules = new LinkedHashMap<>();
        modules.put("core", enabled("core", true));
        modules.put("common", enabled("common", true));
        modules.put("gateway", enabled("gateway", true));
        modules.put("admin", enabled("admin", true));
        modules.put("bizAdmin", enabled("biz-admin", false));

        return ResponseEntity.ok(Map.of(
                "runtime", "CPF_LOCAL_WEB",
                "developmentOnly", true,
                "profiles", environment.getActiveProfiles(),
                "modules", modules,
                "timestamp", Instant.now().toString()));
    }

    private boolean enabled(String name, boolean defaultValue) {
        return environment.getProperty("cpf.local.modules." + name, Boolean.class, defaultValue);
    }
}
