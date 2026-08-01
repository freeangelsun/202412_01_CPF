package com.cpf.reference.online.security.multitenancy;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-38 IntegrationTest — 다중 Tenant 격리·설정·데이터 범위 */
public final class EduDev38IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev38Handler(); }
}
