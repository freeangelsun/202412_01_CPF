package com.cpf.reference.optional.gateway.registry;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-07 RecoveryTest — Service Discovery·Target Failover·복귀 */
public final class EduGw07RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw07Handler(); }
}
