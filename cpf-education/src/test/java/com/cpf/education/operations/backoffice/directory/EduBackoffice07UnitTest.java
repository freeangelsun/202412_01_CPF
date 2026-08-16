package com.cpf.education.operations.backoffice.directory;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-07 UnitTest — 초기 관리자 Bootstrap·첫 로그인·권한 인계 */
public final class EduBackoffice07UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice07Handler(); }
}
