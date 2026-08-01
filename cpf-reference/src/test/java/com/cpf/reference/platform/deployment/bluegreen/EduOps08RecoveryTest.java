package com.cpf.reference.platform.deployment.bluegreen;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-08 RecoveryTest — Blue-Green·Canary 전환·되돌리기 */
public final class EduOps08RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps08Handler(); }
}
