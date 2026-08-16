package com.cpf.education.operations.backoffice.lifecycle;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-09 FailureTest — 입사·이동·휴직·퇴사 Joiner-Mover-Leaver */
public final class EduBackoffice09FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice09Handler(); }
}
