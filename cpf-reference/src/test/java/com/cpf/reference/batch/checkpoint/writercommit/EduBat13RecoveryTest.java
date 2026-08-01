package com.cpf.reference.batch.checkpoint.writercommit;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-13 RecoveryTest — Writer Commit 장애 후 Checkpoint 재시작 */
public final class EduBat13RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat13Handler(); }
}
