package com.cpf.education.operations.backoffice.attachment;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-12 UnitTest — 계정 잠금·비밀번호 초기화·세션 강제 종료 */
public final class EduBackoffice12UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice12Handler(); }
}
