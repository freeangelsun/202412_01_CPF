package com.cpf.backoffice.online;

import com.cpf.backoffice.online.auth.model.BackofficeAdminAccountStatus;
import com.cpf.backoffice.online.management.model.BackofficeEmploymentStatus;

import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BackofficeStatusCatalogTest {
    @Test
    void employmentAndAccountStatusAreDifferentCatalogs() {
        assertEquals(BackofficeEmploymentStatus.EMPLOYED, BackofficeEmploymentStatus.parse("employed"));
        assertTrue(BackofficeEmploymentStatus.SECONDMENT.businessActive());
        assertFalse(BackofficeEmploymentStatus.ON_LEAVE.businessActive());
        assertEquals(BackofficeAdminAccountStatus.PENDING_ACTIVATION,
                BackofficeAdminAccountStatus.parse("pending_activation"));
        assertThrows(CpfValidationException.class, () -> BackofficeEmploymentStatus.parse("ACTIVE"));
        assertThrows(CpfValidationException.class, () -> BackofficeAdminAccountStatus.parse("EMPLOYED"));
    }
}
