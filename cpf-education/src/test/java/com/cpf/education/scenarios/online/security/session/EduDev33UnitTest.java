package com.cpf.education.scenarios.online.security.session;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-33 UnitTest — 인증 Token 만료·갱신·폐기·세션 강제 종료 */
public final class EduDev33UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev33Handler(); }
}
