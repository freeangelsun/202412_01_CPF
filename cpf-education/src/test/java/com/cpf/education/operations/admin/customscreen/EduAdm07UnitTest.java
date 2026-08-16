package com.cpf.education.operations.admin.customscreen;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-07 UnitTest — 고객 전용 화면 추가의 마지막 선택 */
public final class EduAdm07UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm07Handler(); }
}
