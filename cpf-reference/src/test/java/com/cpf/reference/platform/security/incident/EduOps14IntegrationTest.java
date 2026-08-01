package com.cpf.reference.platform.security.incident;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-14 IntegrationTest — 보안 사고·계정·키·세션 긴급 차단 */
public final class EduOps14IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps14Handler(); }
}
