package com.cpf.education.operations.platform.lifecycle.startstop;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-06 RecoveryTest — 기동·종료·Health·Dependency 순서 */
public final class EduOps06RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps06Handler(); }
}
