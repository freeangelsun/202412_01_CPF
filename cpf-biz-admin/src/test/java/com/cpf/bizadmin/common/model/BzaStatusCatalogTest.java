package com.cpf.bizadmin.common.model;

import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BzaStatusCatalogTest {
    @Test
    void employmentAndAccountStatusAreDifferentCatalogs() {
        assertEquals(BzaEmploymentStatus.EMPLOYED, BzaEmploymentStatus.parse("employed"));
        assertTrue(BzaEmploymentStatus.SECONDMENT.businessActive());
        assertFalse(BzaEmploymentStatus.ON_LEAVE.businessActive());
        assertEquals(BzaAdminAccountStatus.PENDING_ACTIVATION,
                BzaAdminAccountStatus.parse("pending_activation"));
        assertThrows(CpfValidationException.class, () -> BzaEmploymentStatus.parse("ACTIVE"));
        assertThrows(CpfValidationException.class, () -> BzaAdminAccountStatus.parse("EMPLOYED"));
    }
}
