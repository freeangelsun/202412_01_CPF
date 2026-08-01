package com.cpf.reference.platform.install.artifact;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-01 IntegrationTest — 신규 환경 설치·Artifact·Checksum 검증 */
public final class EduOps01IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps01Handler(); }
}
