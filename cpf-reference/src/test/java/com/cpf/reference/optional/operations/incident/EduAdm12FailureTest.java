package com.cpf.reference.optional.operations.incident;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-12 redirect/non-executable regression contract. */
public final class EduAdm12FailureTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm12Handler.class));
        assertEquals("EDU-ADM-12", EduAdm12Handler.REDIRECT.requirementId());
        assertEquals("PRODUCT_ADM", EduAdm12Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm12Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm12Handler.REDIRECT.executable());
    }
}
