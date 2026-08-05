package com.cpf.core.common.runtimecontrol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfRuntimeControlPlaneAuditContractTest {
    @Test void arbitraryEvidenceIsStoredAsCanonicalSha256(){
        String value=CpfRuntimeControlPlaneRepository.canonicalAuditEvidence(
                "DELIVERY_UNKNOWN_RESULT","delivery-1:attempt=999");
        assertEquals(64,value.length());
    }

    @Test void existingSha256IsPreservedAndNormalized(){
        String upper="A".repeat(64);
        assertEquals("a".repeat(64),CpfRuntimeControlPlaneRepository.canonicalAuditEvidence("CHANGE_CREATED",upper));
    }

    @Test void rollbackLinkKeepsRecoverableChangeId(){
        String id="123e4567-e89b-12d3-a456-426614174000";
        assertEquals(id,CpfRuntimeControlPlaneRepository.canonicalAuditEvidence("ROLLBACK_OF_CHANGE",id));
    }

    @Test void oversizedRollbackLinkFailsBeforeDatabaseTruncation(){
        assertThrows(IllegalArgumentException.class,()->CpfRuntimeControlPlaneRepository.canonicalAuditEvidence(
                "ROLLBACK_OF_CHANGE","x".repeat(65)));
    }

    @Test void blankEvidenceRemainsNull(){
        assertNull(CpfRuntimeControlPlaneRepository.canonicalAuditEvidence("CHANGE_CREATED","  "));
    }
}
