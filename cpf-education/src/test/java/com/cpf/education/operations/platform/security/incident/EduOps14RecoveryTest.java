package com.cpf.education.operations.platform.security.incident;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-14 RecoveryTest — 보안 사고·계정·키·세션 긴급 차단 */
public final class EduOps14RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps14Handler(); }
}
