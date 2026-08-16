package com.cpf.education.batch.reconcile.requestloss;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-10 RecoveryTest — 실행 요청 응답 유실·결과 대사 */
public final class EduBat10RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat10Handler(); }
}
