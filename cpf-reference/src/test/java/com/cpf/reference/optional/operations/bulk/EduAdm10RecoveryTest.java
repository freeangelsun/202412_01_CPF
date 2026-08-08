package com.cpf.reference.optional.operations.bulk;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-10 redirect/non-executable regression contract. */
public final class EduAdm10RecoveryTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm10Handler.class));
        assertEquals("EDU-ADM-10", EduAdm10Handler.REDIRECT.requirementId());
        assertEquals("PRODUCT_ADM", EduAdm10Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm10Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm10Handler.REDIRECT.executable());
    }
}
