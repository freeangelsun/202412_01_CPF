package com.cpf.reference.optional.gateway.drain;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-09 IntegrationTest — Header 정리·경로·요청·응답 변환 */
public final class EduGw09IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw09Handler(); }
}
