package cpf.bat.operation;

import cpf.bat.job.centercut.BatCenterCutJobConfig;
import cpf.bat.job.failure.BatFailureJobConfig;
import cpf.bat.job.heartbeat.BatHeartbeatJobConfig;
import cpf.bat.job.smoke.BatSmokeJobConfig;
import cpf.pfw.common.batch.CpfBatchFileLogWriter;
import cpf.pfw.common.batch.CpfBatchRuntimeListener;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.pfw.common.logging.ServerInstanceIdentity;
import cpf.pfw.common.logging.file.CpfLogPathPolicy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
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

/**
 * BAT 단독 기동 상태와 smoke Job 실행 결과를 확인하는 운영 API입니다.
 */
@RestController
@Tag(name = "BAT-Operations", description = "BAT 상태, smoke Job 실행, JobInstance 로그 진단 API")
public class BatHealthController {
    private final JdbcTemplate pfwJdbcTemplate;
    private final Environment environment;
    private final BatSmokeOperationService operationService;
    private final BatSmokeExecutionRegistry registry;
    private final ObjectProvider<CpfBatchFileLogWriter> batchFileLogWriterProvider;
    private final ObjectProvider<CpfBatchRuntimeListener> batchRuntimeListenerProvider;

    public BatHealthController(
            @Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate,
            Environment environment,
            BatSmokeOperationService operationService,
            BatSmokeExecutionRegistry registry,
            ObjectProvider<CpfBatchFileLogWriter> batchFileLogWriterProvider,
            ObjectProvider<CpfBatchRuntimeListener> batchRuntimeListenerProvider) {
        this.pfwJdbcTemplate = pfwJdbcTemplate;
        this.environment = environment;
        this.operationService = operationService;
        this.registry = registry;
        this.batchFileLogWriterProvider = batchFileLogWriterProvider;
        this.batchRuntimeListenerProvider = batchRuntimeListenerProvider;
    }

    @GetMapping("/bat/api/health")
    @Operation(operationId = "getBatHealth", summary = "BAT 실행 상태 조회")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        ServerInstanceIdentity.Identity identity = ServerInstanceIdentity.current();
        response.put("status", "UP");
        response.put("application", "bat");
        response.put("serverInstanceId", identity.serverInstanceId());
        response.put("workerId", identity.serverInstanceId());
        response.put("profiles", environment.getActiveProfiles());
        response.put("database", checkDatabase());
        response.put("smoke", registry.snapshot());
        response.put("supportedJobs", new String[] {
                BatSmokeJobConfig.SMOKE_JOB_ID,
                BatHeartbeatJobConfig.JOB_ID,
                BatFailureJobConfig.JOB_ID,
                BatCenterCutJobConfig.JOB_ID
        });
        return ResponseEntity.ok(response);
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
        CpfLogPathPolicy pathPolicy = new CpfLogPathPolicy(environment);
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

    private Map<String, Object> checkDatabase() {
        try {
            Integer value = pfwJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Map.of("status", value != null && value == 1 ? "UP" : "UNKNOWN");
        } catch (Exception ex) {
            return Map.of("status", "DOWN", "message", ex.getClass().getSimpleName());
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
