package com.cpf.reference.online.security.session;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-33 FailureTest — 인증 Token 만료·갱신·폐기·세션 강제 종료 */
public final class EduDev33FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev33Handler(); }
}
