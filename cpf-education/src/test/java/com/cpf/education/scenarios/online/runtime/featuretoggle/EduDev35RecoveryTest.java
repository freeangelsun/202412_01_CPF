package com.cpf.education.scenarios.online.runtime.featuretoggle;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-35 RecoveryTest — 기능 전환 Canary·Kill Switch·사용자 Segment */
public final class EduDev35RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev35Handler(); }
}
