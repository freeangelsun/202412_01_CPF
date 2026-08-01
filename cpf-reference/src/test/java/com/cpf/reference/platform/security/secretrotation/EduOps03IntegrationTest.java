package com.cpf.reference.platform.security.secretrotation;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-03 IntegrationTest — Secret·Certificate 배포·교체·만료 대응 */
public final class EduOps03IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps03Handler(); }
}
