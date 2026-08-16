package com.cpf.education.operations.admin.command;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-03 UnitTest — 안전한 운영 조치 */
public final class EduAdm03UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm03Handler(); }
}
