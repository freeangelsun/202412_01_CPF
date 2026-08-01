package com.cpf.reference.optional.gateway.publish;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-05 IntegrationTest — Draft·검증·승인·게시·부분 적용 */
public final class EduGw05IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw05Handler(); }
}
