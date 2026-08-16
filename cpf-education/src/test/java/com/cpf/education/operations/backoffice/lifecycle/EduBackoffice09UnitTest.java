package com.cpf.education.operations.backoffice.lifecycle;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-09 UnitTest — 입사·이동·휴직·퇴사 Joiner-Mover-Leaver */
public final class EduBackoffice09UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice09Handler(); }
}
