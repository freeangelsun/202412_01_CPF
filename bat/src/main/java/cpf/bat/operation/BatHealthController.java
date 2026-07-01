package cpf.bat.operation;

import cpf.bat.job.BatSmokeJobConfig;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.logging.ServerInstanceIdentity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BAT 단독 기동 상태와 smoke Job 실행 결과를 확인하는 운영 API입니다.
 */
@RestController
public class BatHealthController {
    private final JdbcTemplate pfwJdbcTemplate;
    private final Environment environment;
    private final BatSmokeOperationService operationService;
    private final BatSmokeExecutionRegistry registry;

    public BatHealthController(
            @Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate,
            Environment environment,
            BatSmokeOperationService operationService,
            BatSmokeExecutionRegistry registry) {
        this.pfwJdbcTemplate = pfwJdbcTemplate;
        this.environment = environment;
        this.operationService = operationService;
        this.registry = registry;
    }

    @GetMapping("/bat/api/health")
    @CpfTransaction(id = "BAT01OPR0001", name = "BATHealth")
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
                BatSmokeJobConfig.HEARTBEAT_JOB_ID,
                BatSmokeJobConfig.FAIL_JOB_ID,
                BatSmokeJobConfig.CENTER_CUT_JOB_ID
        });
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bat/api/smoke/jobs/{jobId}/run")
    @CpfTransaction(id = "BAT02OPR0002", name = "BATSmokeJobRun")
    public ResponseEntity<Map<String, Object>> runSmokeJob(@PathVariable String jobId) {
        Map<String, Object> result = operationService.run(jobId, "BAT smoke API 수동 실행");
        if (BatSmokeJobConfig.FAIL_JOB_ID.equals(jobId)) {
            registry.recordFailure(result);
        } else {
            registry.recordSuccess(result);
        }
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> checkDatabase() {
        try {
            Integer value = pfwJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Map.of("status", value != null && value == 1 ? "UP" : "UNKNOWN");
        } catch (Exception ex) {
            return Map.of("status", "DOWN", "message", ex.getClass().getSimpleName());
        }
    }
}
