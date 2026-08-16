package com.cpf.education.operations.admin.reuse;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-01 UnitTest — 기존 ADM 기능 재사용 판단 */
public final class EduAdm01UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm01Handler(); }
}
