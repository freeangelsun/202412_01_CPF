package com.cpf.reference.optional.operations.detail;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** EDU-ADM-09 redirect/non-executable regression contract. */
public final class EduAdm09ConcurrencyTest {
    @Test
    void productOwnedOrMergedScenarioIsNotAnExecutableReferenceHandler() {
        assertFalse(AbstractEduCapabilityHandler.class.isAssignableFrom(EduAdm09Handler.class));
        assertEquals("EDU-ADM-09", EduAdm09Handler.REDIRECT.requirementId());
        assertEquals("MERGE_EDU", EduAdm09Handler.REDIRECT.architectureDecision());
        assertEquals("CPF_ADM_OPERATOR", EduAdm09Handler.REDIRECT.requiredRole());
        assertFalse(EduAdm09Handler.REDIRECT.executable());
    }
}
