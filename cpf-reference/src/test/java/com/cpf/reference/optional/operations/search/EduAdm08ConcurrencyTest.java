package com.cpf.reference.optional.operations.search;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-08 redirect/non-executable regression contract. */
public final class EduAdm08ConcurrencyTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm08Handler.class));
        assertEquals("EDU-ADM-08", EduAdm08Handler.REDIRECT.requirementId());
        assertEquals("PRODUCT_ADM", EduAdm08Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm08Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm08Handler.REDIRECT.executable());
    }
}
