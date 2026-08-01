package com.cpf.reference.batch.faulttolerance.retryskip;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-12 RecoveryTest — Retry·Skip·No-Skip 예외 분류 */
public final class EduBat12RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat12Handler(); }
}
