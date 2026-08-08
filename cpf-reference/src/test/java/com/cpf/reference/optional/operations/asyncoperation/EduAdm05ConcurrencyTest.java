package com.cpf.reference.optional.operations.asyncoperation;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-05 redirect/non-executable regression contract. */
public final class EduAdm05ConcurrencyTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm05Handler.class));
        assertEquals("EDU-ADM-05", EduAdm05Handler.REDIRECT.requirementId());
        assertEquals("MERGE_EDU", EduAdm05Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm05Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm05Handler.REDIRECT.executable());
    }
}
