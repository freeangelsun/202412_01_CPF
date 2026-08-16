package com.cpf.education.scenarios.online.security.authorization;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-11 UnitTest — 권한·데이터 범위·개인정보 가림·감사 */
public final class EduDev11UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev11Handler(); }
}
