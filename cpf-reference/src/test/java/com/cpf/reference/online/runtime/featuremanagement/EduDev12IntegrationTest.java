package com.cpf.reference.online.runtime.featuremanagement;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-12 IntegrationTest — Cache·기능 전환·Secret 교체 */
public final class EduDev12IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev12Handler(); }
}
