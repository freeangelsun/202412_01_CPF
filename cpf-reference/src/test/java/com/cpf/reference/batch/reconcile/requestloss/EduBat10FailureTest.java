package com.cpf.reference.batch.reconcile.requestloss;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-10 FailureTest — 실행 요청 응답 유실·결과 대사 */
public final class EduBat10FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat10Handler(); }
}
