package com.cpf.reference.optional.operations.session;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-ADM-17 IntegrationTest — Browser 세션 만료·재로그인·위험 조치 안전성 */
public final class EduAdm17IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm17Handler(); }
}
