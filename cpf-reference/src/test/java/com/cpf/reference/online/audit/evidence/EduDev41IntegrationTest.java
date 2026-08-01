package com.cpf.reference.online.audit.evidence;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-41 IntegrationTest — 감사 증적 Export·무결성 Hash·검증 */
public final class EduDev41IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev41Handler(); }
}
