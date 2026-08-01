package com.cpf.reference.online.security.session;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-33 ConcurrencyTest — 인증 Token 만료·갱신·폐기·세션 강제 종료 */
public final class EduDev33ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev33Handler(); }
}
