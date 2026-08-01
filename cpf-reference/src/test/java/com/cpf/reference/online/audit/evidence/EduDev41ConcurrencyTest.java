package com.cpf.reference.online.audit.evidence;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-41 ConcurrencyTest — 감사 증적 Export·무결성 Hash·검증 */
public final class EduDev41ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev41Handler(); }
}
