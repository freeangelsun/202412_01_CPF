package com.cpf.education.operations.platform.deployment.rolling;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-07 RecoveryTest — Rolling 배포·Session·Connection Drain */
public final class EduOps07RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps07Handler(); }
}
