package com.cpf.education.operations.backoffice.approvalhistory;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-11 RecoveryTest — 위임 중첩·기간 만료·결재 경로 재계산 */
public final class EduBackoffice11RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice11Handler(); }
}
