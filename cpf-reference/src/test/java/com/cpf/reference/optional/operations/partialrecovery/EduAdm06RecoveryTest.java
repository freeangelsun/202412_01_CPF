package com.cpf.reference.optional.operations.partialrecovery;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-06 redirect/non-executable regression contract. */
public final class EduAdm06RecoveryTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm06Handler.class));
        assertEquals("EDU-ADM-06", EduAdm06Handler.REDIRECT.requirementId());
        assertEquals("MERGE_EDU", EduAdm06Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm06Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm06Handler.REDIRECT.executable());
    }
}
