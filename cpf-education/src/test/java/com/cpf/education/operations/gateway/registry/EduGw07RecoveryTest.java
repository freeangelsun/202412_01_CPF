package com.cpf.education.operations.gateway.registry;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-07 RecoveryTest — Service Discovery·Target Failover·복귀 */
public final class EduGw07RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw07Handler(); }
}
