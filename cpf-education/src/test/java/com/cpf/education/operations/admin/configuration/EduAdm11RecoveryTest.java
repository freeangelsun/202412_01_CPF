package com.cpf.education.operations.admin.configuration;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-11 RecoveryTest — 설정·기능전환·유지보수 창 운영 */
public final class EduAdm11RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm11Handler(); }
}
