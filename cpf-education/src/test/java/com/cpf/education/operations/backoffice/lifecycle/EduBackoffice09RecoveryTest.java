package com.cpf.education.operations.backoffice.lifecycle;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-09 RecoveryTest — 입사·이동·휴직·퇴사 Joiner-Mover-Leaver */
public final class EduBackoffice09RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice09Handler(); }
}
