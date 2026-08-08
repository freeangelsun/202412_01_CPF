package com.cpf.reference.optional.operations.correlation;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-15 redirect/non-executable regression contract. */
public final class EduAdm15FailureTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm15Handler.class));
        assertEquals("EDU-ADM-15", EduAdm15Handler.REDIRECT.requirementId());
        assertEquals("PRODUCT_ADM", EduAdm15Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm15Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm15Handler.REDIRECT.executable());
    }
}
