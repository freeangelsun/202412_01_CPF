package com.cpf.reference.online.servicecall.topology;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-06 IntegrationTest — 같은 애플리케이션·분리 서비스 호출 동등성 */
public final class EduDev06IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev06Handler(); }
}
