package com.cpf.reference.optional.gateway.audit;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-13 RecoveryTest — Canary·가중치 Routing·Version Rollback */
public final class EduGw13RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw13Handler(); }
}
