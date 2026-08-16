package com.cpf.education.operations.platform.deployment.bluegreen;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-08 RecoveryTest — Blue-Green·Canary 전환·되돌리기 */
public final class EduOps08RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps08Handler(); }
}
