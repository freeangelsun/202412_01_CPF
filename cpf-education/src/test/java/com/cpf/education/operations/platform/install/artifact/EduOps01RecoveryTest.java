package com.cpf.education.operations.platform.install.artifact;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-01 RecoveryTest — 신규 환경 설치·Artifact·Checksum 검증 */
public final class EduOps01RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps01Handler(); }
}
