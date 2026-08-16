package com.cpf.education.operations.platform.install.artifact;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-01 IntegrationTest — 신규 환경 설치·Artifact·Checksum 검증 */
public final class EduOps01IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps01Handler(); }
}
