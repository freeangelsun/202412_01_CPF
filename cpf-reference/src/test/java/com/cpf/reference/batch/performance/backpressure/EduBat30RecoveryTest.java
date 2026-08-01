package com.cpf.reference.batch.performance.backpressure;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-30 RecoveryTest — 대용량 처리 성능·용량·Backpressure */
public final class EduBat30RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat30Handler(); }
}
