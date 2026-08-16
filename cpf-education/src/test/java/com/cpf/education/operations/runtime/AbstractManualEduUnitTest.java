package com.cpf.education.operations.runtime;
import com.cpf.education.operations.runtime.application.EduValidationException;
import com.cpf.education.operations.runtime.model.EduFailurePoint;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractManualEduUnitTest extends AbstractManualEduTestSupport {
    @Test void definitionAndManualContractAreExecutable() {
        var definition = handler().definition();
        assertFalse(definition.requiredFields().isEmpty());
        assertTrue(definition.steps().size() >= 5);
        assertFalse(definition.supportedFailures().isEmpty());
        assertEquals(handler().getClass().getPackageName(), handler().implementationPackage());
        assertFalse(handler().businessStates().isEmpty());
        assertFalse(handler().exceptionScenarios().isEmpty());
        assertFalse(handler().requiredVerification().isEmpty());
        assertDoesNotThrow(() -> handler().validate(command(EduFailurePoint.NONE)));
    }

    @Test void rejectsRequirementSpecificInvalidPayload() {
        var invalid = handler().invalidPayloadExample(validPayload());
        assertThrows(EduValidationException.class, () -> handler().validate(commandWithPayload(invalid)));
    }

    @Test void rejectsMissingRoleAndDataScope() {
        var valid = command(EduFailurePoint.NONE);
        var noRole = new com.cpf.education.operations.runtime.model.EduExecutionCommand(valid.businessKey(),
                valid.idempotencyKey(), valid.expectedVersion(), valid.actorId(), java.util.Set.of(),
                valid.dataScope(), valid.requestReason(), valid.requestId(), valid.traceId(), valid.payload(),
                valid.failurePoint(), valid.autoApprove(), valid.autoAcknowledge());
        assertThrows(com.cpf.education.operations.runtime.application.EduAuthorizationException.class,
                () -> handler().validate(noRole));
    }
}
