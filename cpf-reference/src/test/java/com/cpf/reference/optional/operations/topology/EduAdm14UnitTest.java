package com.cpf.reference.optional.operations.topology;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-14 redirect/non-executable regression contract. */
public final class EduAdm14UnitTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm14Handler.class));
        assertEquals("EDU-ADM-14", EduAdm14Handler.REDIRECT.requirementId());
        assertEquals("PRODUCT_ADM", EduAdm14Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm14Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm14Handler.REDIRECT.executable());
    }
}
