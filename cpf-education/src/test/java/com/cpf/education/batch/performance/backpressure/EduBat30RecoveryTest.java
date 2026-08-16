package com.cpf.education.batch.performance.backpressure;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-30 RecoveryTest — 대용량 처리 성능·용량·Backpressure */
public final class EduBat30RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat30Handler(); }
}
