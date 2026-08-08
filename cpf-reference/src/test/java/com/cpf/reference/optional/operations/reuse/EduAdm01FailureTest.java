package com.cpf.reference.optional.operations.reuse;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-01 redirect/non-executable regression contract. */
public final class EduAdm01FailureTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm01Handler.class));
        assertEquals("EDU-ADM-01", EduAdm01Handler.REDIRECT.requirementId());
        assertEquals("MERGE_EDU", EduAdm01Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm01Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm01Handler.REDIRECT.executable());
    }
}
