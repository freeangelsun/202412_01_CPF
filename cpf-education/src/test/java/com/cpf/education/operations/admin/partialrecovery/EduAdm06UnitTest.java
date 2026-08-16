package com.cpf.education.operations.admin.partialrecovery;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-06 UnitTest — 부분 성공·대상별 복구 */
public final class EduAdm06UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm06Handler(); }
}
