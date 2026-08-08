package com.cpf.reference.optional.operations.evidence;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-13 redirect/non-executable regression contract. */
public final class EduAdm13IntegrationTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm13Handler.class));
        assertEquals("EDU-ADM-13", EduAdm13Handler.REDIRECT.requirementId());
        assertEquals("PRODUCT_ADM", EduAdm13Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm13Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm13Handler.REDIRECT.executable());
    }
}
