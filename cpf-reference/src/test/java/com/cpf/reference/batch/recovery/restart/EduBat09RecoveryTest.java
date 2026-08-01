package com.cpf.reference.batch.recovery.restart;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-09 RecoveryTest — 중지·재시작·실패건 재처리 */
public final class EduBat09RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat09Handler(); }
}
