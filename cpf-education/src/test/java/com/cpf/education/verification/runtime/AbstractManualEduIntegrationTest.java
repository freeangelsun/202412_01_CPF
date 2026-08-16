package com.cpf.education.verification.runtime;

import com.cpf.education.operations.runtime.model.EduExecutionState;
import com.cpf.education.operations.runtime.model.EduFailurePoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractManualEduIntegrationTest extends AbstractManualEduTestSupport {
    @TempDir Path directory;

    @Test void persistsNormalExecutionAndAudit() {
        var execution = service(directory);
        var result = execution.execute(handler().definition().requirementId(), command(EduFailurePoint.NONE));
        if (result.state() == EduExecutionState.WAITING_EXTERNAL) {
            result = execution.acknowledgeExternal(result.operationId(), "operator", "integration acknowledgement");
        }
        assertEquals(EduExecutionState.SUCCEEDED, result.state());
        assertFalse(execution.audits(result.operationId()).isEmpty());
        assertFalse(execution.targets(result.operationId()).isEmpty());
        var restarted = service(directory);
        assertEquals(result.operationId(), restarted.require(result.operationId()).operationId());
    }
}
