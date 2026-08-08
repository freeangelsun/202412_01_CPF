package com.cpf.reference.optional.operations.session;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-17 redirect/non-executable regression contract. */
public final class EduAdm17ConcurrencyTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm17Handler.class));
        assertEquals("EDU-ADM-17", EduAdm17Handler.REDIRECT.requirementId());
        assertEquals("PRODUCT_ADM", EduAdm17Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm17Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm17Handler.REDIRECT.executable());
    }
}
