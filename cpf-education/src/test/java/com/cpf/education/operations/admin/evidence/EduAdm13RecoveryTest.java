package com.cpf.education.operations.admin.evidence;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-13 RecoveryTest — 감사 증적·다운로드·승인 반출 */
public final class EduAdm13RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm13Handler(); }
}
