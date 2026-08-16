package com.cpf.education.scenarios.online.runtime.featuremanagement;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-12 RecoveryTest — Cache·기능 전환·Secret 교체 */
public final class EduDev12RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev12Handler(); }
}
