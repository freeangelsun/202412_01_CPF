package com.cpf.education.batch.recovery.restart;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-09 FailureTest — 중지·재시작·실패건 재처리 */
public final class EduBat09FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat09Handler(); }
}
