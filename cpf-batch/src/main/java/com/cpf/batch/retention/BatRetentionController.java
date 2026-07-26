package com.cpf.batch.retention;

import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.retention.CpfRetentionCommand;
import com.cpf.core.api.retention.CpfRetentionPolicy;
import com.cpf.core.api.retention.CpfRetentionResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;

/** 위험한 보존/삭제 작업의 BAT 운영 API. 실제 파괴 실행은 기본 OFF kill switch로 보호합니다. */
@RestController
@RequestMapping("/bat/api/retention")
public class BatRetentionController extends com.cpf.batch.common.base.BatBaseController {
    private final BatRetentionOperations operations;
    private final Environment environment;
    public BatRetentionController(BatRetentionOperations operations, Environment environment) { this.operations = operations; this.environment = environment; }

    @GetMapping("/targets")
    public ResponseEntity<?> targets() { return ResponseEntity.ok(operations.targets()); }

    @PostMapping("/execute")
    @CpfOnlineTransaction(id = "OBATRT0001", name = "BatRetentionExecute")
    public ResponseEntity<?> execute(@RequestBody Request request, HttpServletRequest http) {
        Principal principal = http.getUserPrincipal();
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) return ResponseEntity.status(401).body(Map.of("message","인증 사용자가 필요합니다."));
        String action = request.action() == null ? "KEEP" : request.action().trim().toUpperCase();
        boolean destructive = ("ARCHIVE".equals(action) || "PURGE".equals(action)) && !request.dryRun() && !request.legalHold();
        if (destructive && request.cutoff() == null) return ResponseEntity.badRequest().body(Map.of("message","실제 ARCHIVE/PURGE는 cutoff가 필수입니다."));
        if (destructive && !environment.getProperty("cpf.retention.execute-enabled", Boolean.class, false)) return ResponseEntity.status(403).body(Map.of("message","cpf.retention.execute-enabled=true일 때만 파괴 작업을 실행할 수 있습니다."));
        CpfRetentionResult result = operations.execute(new CpfRetentionCommand(
                new CpfRetentionPolicy(request.target(), action, request.legalHold(), request.dryRun()),
                request.cutoff(), principal.getName(), request.reason()));
        return ResponseEntity.ok(result);
    }

    public record Request(String target, String action, Instant cutoff, boolean dryRun, boolean legalHold, String reason) {}
}
