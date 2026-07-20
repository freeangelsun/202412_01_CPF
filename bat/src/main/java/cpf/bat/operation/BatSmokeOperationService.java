package cpf.bat.operation;

import cpf.pfw.common.batch.CpfBatchExecutionRequest;
import cpf.pfw.common.batch.CpfBatchExecutionResult;
import cpf.pfw.common.batch.CpfBatchLauncher;
import cpf.pfw.common.batch.CpfBatchOperationRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BAT smoke API가 PFW Batch 공통 실행 Facade를 통해 Job을 실행하도록 연결합니다.
 */
@Service
public class BatSmokeOperationService extends cpf.bat.common.base.BatBaseService {
    private static final String REQUEST_USER = "BAT_SMOKE";

    private final CpfBatchLauncher batchLauncher;
    private final CpfBatchOperationRepository repository;

    public BatSmokeOperationService(CpfBatchLauncher batchLauncher, CpfBatchOperationRepository repository) {
        this.batchLauncher = batchLauncher;
        this.repository = repository;
    }

    public Map<String, Object> run(String jobId, String reason) {
        CpfBatchExecutionRequest request = CpfBatchExecutionRequest.run(
                jobId,
                "{}",
                REQUEST_USER,
                hasText(reason) ? reason : "BAT smoke 실행 검증");
        CpfBatchExecutionResult result = batchLauncher.run(request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("executed", result.executed());
        response.put("jobId", result.jobId());
        response.put("pfwExecutionId", result.pfwExecutionId());
        response.put("springBatchExecutionId", result.springBatchExecutionId());
        response.put("status", result.status());
        response.put("message", result.message());
        response.put("detail", loadExecutionDetail(result.pfwExecutionId()));
        return response;
    }

    private Map<String, Object> loadExecutionDetail(Long pfwExecutionId) {
        if (pfwExecutionId == null || pfwExecutionId < 1 || !repository.available()) {
            return Map.of();
        }
        return repository.findExecutionDetail(pfwExecutionId);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
