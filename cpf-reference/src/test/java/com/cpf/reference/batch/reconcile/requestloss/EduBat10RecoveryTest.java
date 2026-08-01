package com.cpf.reference.batch.reconcile.requestloss;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-10 RecoveryTest — 실행 요청 응답 유실·결과 대사 */
public final class EduBat10RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat10Handler(); }
}
