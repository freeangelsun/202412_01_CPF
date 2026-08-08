package com.cpf.reference.optional.operations.configuration;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-11 redirect/non-executable regression contract. */
public final class EduAdm11FailureTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm11Handler.class));
        assertEquals("EDU-ADM-11", EduAdm11Handler.REDIRECT.requirementId());
        assertEquals("PRODUCT_ADM", EduAdm11Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm11Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm11Handler.REDIRECT.executable());
    }
}
