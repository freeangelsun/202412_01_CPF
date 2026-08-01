package com.cpf.reference.optional.backoffice.separationofduties;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-10 IntegrationTest — 역할 충돌·직무분리·실효 권한 Simulation */
public final class EduBackoffice10IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice10Handler(); }
}
