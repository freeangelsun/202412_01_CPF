package com.cpf.reference.edu.runtime;

import com.cpf.reference.edu.runtime.model.EduExecutionState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractManualEduFailureTest extends AbstractManualEduTestSupport {
    @TempDir Path directory;

    @Test void persistsFailureState() {
        var result = service(directory).execute(handler().definition().requirementId(), command(failure()));
        assertNotEquals(EduExecutionState.SUCCEEDED, result.state());
        assertTrue(result.state() == EduExecutionState.FAILED_RETRYABLE
                || result.state() == EduExecutionState.UNKNOWN_RESULT
                || result.state() == EduExecutionState.PARTIAL_SUCCESS);
    }
}
