package com.cpf.education.operations.admin.approval;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-04 UnitTest — 승인 필요한 위험 조치 */
public final class EduAdm04UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm04Handler(); }
}
