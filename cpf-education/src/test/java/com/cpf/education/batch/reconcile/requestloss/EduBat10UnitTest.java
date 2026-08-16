package com.cpf.education.batch.reconcile.requestloss;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-10 UnitTest — 실행 요청 응답 유실·결과 대사 */
public final class EduBat10UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat10Handler(); }
}
