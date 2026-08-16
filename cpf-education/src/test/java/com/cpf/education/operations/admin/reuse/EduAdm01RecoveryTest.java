package com.cpf.education.operations.admin.reuse;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-01 RecoveryTest — 기존 ADM 기능 재사용 판단 */
public final class EduAdm01RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm01Handler(); }
}
