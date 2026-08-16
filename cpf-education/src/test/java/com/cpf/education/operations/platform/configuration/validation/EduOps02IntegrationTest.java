package com.cpf.education.operations.platform.configuration.validation;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-02 IntegrationTest — Profile·환경변수·설정값 전체 검증 */
public final class EduOps02IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps02Handler(); }
}
