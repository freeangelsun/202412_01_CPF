package com.cpf.batch.operation;

import com.cpf.batch.job.centercut.BatCenterCutJobConfig;
import com.cpf.batch.job.failure.BatFailureJobConfig;
import com.cpf.batch.job.heartbeat.BatHeartbeatJobConfig;
import com.cpf.batch.job.smoke.BatSmokeJobConfig;
import com.cpf.batch.runtime.BatBatchFileLogWriter;
import com.cpf.batch.runtime.BatBatchRuntimeListener;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.logging.CpfServerIdentity;
import com.cpf.core.api.logging.CpfLogPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.OffsetDateTime;

/**
 * BAT 단독 기동 상태와 smoke Job 실행 결과를 확인하는 운영 API입니다.
 */
@RestController
@Tag(name = "BAT-Operations", description = "BAT 상태, smoke Job 실행, JobInstance 로그 진단 API")
public class BatHealthController extends com.cpf.batch.common.base.BatBaseController {
    private final JdbcTemplate batJdbcTemplate;
    private final Environment environment;
    private final BatSmokeOperationService operationService;
    private final BatSmokeExecutionRegistry registry;
    private final ObjectProvider<BatBatchFileLogWriter> batchFileLogWriterProvider;
    private final ObjectProvider<BatBatchRuntimeListener> batchRuntimeListenerProvider;

    public BatHealthController(
            @Qualifier("batJdbcTemplate") JdbcTemplate batJdbcTemplate,
            Environment environment,
            BatSmokeOperationService operationService,
            BatSmokeExecutionRegistry registry,
            ObjectProvider<BatBatchFileLogWriter> batchFileLogWriterProvider,
            ObjectProvider<BatBatchRuntimeListener> batchRuntimeListenerProvider) {
        this.batJdbcTemplate = batJdbcTemplate;
        this.environment = environment;
        this.operationService = operationService;
        this.registry = registry;
        this.batchFileLogWriterProvider = batchFileLogWriterProvider;
        this.batchRuntimeListenerProvider = batchRuntimeListenerProvider;
    }

    @GetMapping("/bat/api/health/liveness")
    @Operation(operationId = "getBatLiveness", summary = "BAT Liveness 조회")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> response = baseHealthResponse("UP");
        response.put("process", "UP");
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/bat/api/health", "/bat/api/health/readiness"})
    @Operation(operationId = "getBatReadiness", summary = "BAT Readiness 조회")
    public ResponseEntity<Map<String, Object>> health() {
        String database = checkDatabase();
        boolean runtimeReady = batchRuntimeListenerProvider.getIfAvailable() != null;
        boolean ready = "UP".equals(database) && runtimeReady;
        Map<String, Object> response = baseHealthResponse(ready ? "UP" : "DOWN");
        response.put("database", database);
        response.put("runtimeListener", runtimeReady ? "UP" : "DOWN");
        response.put("smoke", registry.snapshot());
        response.put("supportedJobs", new String[] {
                BatSmokeJobConfig.SMOKE_JOB_ID,
                BatHeartbeatJobConfig.JOB_ID,
                BatFailureJobConfig.JOB_ID,
                BatCenterCutJobConfig.JOB_ID
        });
        return ResponseEntity.status(ready ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    private Map<String, Object> baseHealthResponse(String status) {
        Map<String, Object> response = new LinkedHashMap<>();
        CpfServerIdentity.Identity identity = CpfServerIdentity.current();
        response.put("status", status);
        response.put("application", "bat");
        response.put("moduleId", environment.getProperty("cpf.framework.module-id", "BAT"));
        response.put("wasId", environment.getProperty("cpf.framework.was-id", "batWK01"));
        response.put("serverInstanceId", identity.serverInstanceId());
        response.put("workerId", identity.serverInstanceId());
        response.put("host", identity.hostName());
        response.put("hostName", identity.hostName());
        response.put("processId", identity.processId());
        response.put("profiles", environment.getActiveProfiles());
        response.put("checkedAt", OffsetDateTime.now().toString());
        return response;
    }

    @PostMapping("/bat/api/smoke/jobs/{jobId}/run")
    @CpfOnlineTransaction(id = "OBATOP0002", name = "BATSmokeJobRun")
    @Operation(operationId = "runBatSmokeJob", summary = "BAT smoke Job 수동 실행")
    public ResponseEntity<Map<String, Object>> runSmokeJob(@PathVariable String jobId) {
        Map<String, Object> result = operationService.run(jobId, "BAT smoke API 수동 실행");
        if (BatFailureJobConfig.JOB_ID.equals(jobId)) {
            registry.recordFailure(result);
        } else {
            registry.recordSuccess(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/bat/api/diagnostics/logging")
    @CpfOnlineTransaction(id = "OBATOP0003", name = "BATLoggingDiagnostics")
    @Operation(operationId = "getBatLoggingDiagnostics", summary = "BAT JobInstance 로그 설정 진단")
    public ResponseEntity<Map<String, Object>> loggingDiagnostics() {
        Map<String, Object> response = new LinkedHashMap<>();
        CpfLogPaths pathPolicy = new CpfLogPaths(environment);
        Path basePath = pathPolicy.logRoot();
        Path logDirectory = pathPolicy.batchJobLogPath(Path.of("bat"));
        Path jobsDirectory = logDirectory.resolve("jobs");

        response.put("application", "bat");
        response.put("profiles", environment.getActiveProfiles());
        response.put("cpfBatchFileLogWriterBean", batchFileLogWriterProvider.getIfAvailable() != null);
        response.put("cpfBatchRuntimeListenerBean", batchRuntimeListenerProvider.getIfAvailable() != null);
        response.put("jobListenerWiring", Map.of(
                "smokeJob", BatSmokeJobConfig.SMOKE_JOB_ID,
                "smokeStep", BatSmokeJobConfig.SMOKE_STEP_ID,
                "centerCutJob", BatCenterCutJobConfig.JOB_ID,
                "requiredListenerBean", "cpfBatchRuntimeListener"));
        response.put("properties", Map.of(
                "cpf.logging.file.enabled", environment.getProperty("cpf.logging.file.enabled", "true"),
                "cpf.logging.file.batch-enabled", environment.getProperty("cpf.logging.file.batch-enabled", "true"),
                "cpf.logging.file.base-path", basePath.toString(),
                "cpf.environment", pathPolicy.environmentCode(),
                "cpf.framework.instance-id", pathPolicy.instanceId(),
                "cpf.logging.file.timezone", environment.getProperty("cpf.logging.file.timezone", "Asia/Seoul"),
                "server.port", environment.getProperty("server.port", "8093"),
                "cpf.framework.module-id", environment.getProperty("cpf.framework.module-id", "BAT"),
                "cpf.framework.was-id", environment.getProperty("cpf.framework.was-id", "batWK01")));
        response.put("workingDirectory", Path.of("").toAbsolutePath().normalize().toString());
        response.put("logDirectory", logDirectory.toString());
        response.put("jobInstanceLogRoot", jobsDirectory.toString());
        response.put("jobInstanceLogPattern",
                "{environment}/bat/jobs/{businessDate}/{jobName}/cpf-bat-{jobName}-{jobInstanceId}-{businessDate}.log");
        response.put("logDirectoryExists", Files.exists(logDirectory));
        response.put("logDirectoryWritable", isWritableDirectory(logDirectory));
        response.put("jobInstanceLogCount", countLogFiles(jobsDirectory));
        return ResponseEntity.ok(response);
    }

    private String checkDatabase() {
        try {
            Integer value = batJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Integer.valueOf(1).equals(value) ? "UP" : "DOWN";
        } catch (Exception ex) {
            return "DOWN";
        }
    }

    private boolean isWritableDirectory(Path logDirectory) {
        try {
            Files.createDirectories(logDirectory);
            return Files.isWritable(logDirectory);
        } catch (Exception ex) {
            return false;
        }
    }

    private long countLogFiles(Path logDirectory) {
        try {
            if (!Files.isDirectory(logDirectory)) {
                return 0L;
            }
            try (var paths = Files.walk(logDirectory)) {
                return paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".log"))
                        .count();
            }
        } catch (Exception ex) {
            return -1L;
        }
    }
}
