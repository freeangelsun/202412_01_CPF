package com.cpf.reference.optional.operations.configuration;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-ADM-11 IntegrationTest — 설정·기능전환·유지보수 창 운영 */
public final class EduAdm11IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm11Handler(); }
}
