package com.cpf.reference.optional.operations.notification;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-16 redirect/non-executable regression contract. */
public final class EduAdm16RecoveryTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm16Handler.class));
        assertEquals("EDU-ADM-16", EduAdm16Handler.REDIRECT.requirementId());
        assertEquals("PRODUCT_ADM", EduAdm16Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm16Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm16Handler.REDIRECT.executable());
    }
}
