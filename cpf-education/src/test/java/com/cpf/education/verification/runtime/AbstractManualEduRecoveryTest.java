package com.cpf.education.verification.runtime;

import com.cpf.education.operations.runtime.model.EduExecutionState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractManualEduRecoveryTest extends AbstractManualEduTestSupport {
    @TempDir Path directory;

    @Test void recoversAfterInjectedFailure() {
        var execution = service(directory);
        var failed = execution.execute(handler().definition().requirementId(), command(failure()));
        var recovered = failed.state() == EduExecutionState.PARTIAL_SUCCESS
                ? execution.reconcile(failed.operationId(), "operator", "reconcile test")
                : execution.retry(failed.operationId(), "operator", "retry test");
        if (recovered.state() == EduExecutionState.WAITING_EXTERNAL
                || recovered.state() == EduExecutionState.UNKNOWN_RESULT
                || recovered.state() == EduExecutionState.RECONCILING) {
            recovered = execution.acknowledgeExternal(recovered.operationId(), "operator", "recovery acknowledgement");
        }
        assertEquals(EduExecutionState.SUCCEEDED, recovered.state());
    }
}
