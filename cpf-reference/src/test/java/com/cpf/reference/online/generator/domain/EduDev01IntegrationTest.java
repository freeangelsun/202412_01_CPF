package com.cpf.reference.online.generator.domain;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-01 IntegrationTest — Generator 기반 신규 업무 영역 생성 */
public final class EduDev01IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev01Handler(); }
}
