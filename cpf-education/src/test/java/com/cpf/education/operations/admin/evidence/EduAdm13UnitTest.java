package com.cpf.education.operations.admin.evidence;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-13 UnitTest — 감사 증적·다운로드·승인 반출 */
public final class EduAdm13UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm13Handler(); }
}
