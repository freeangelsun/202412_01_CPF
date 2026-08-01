package com.cpf.reference.batch.chunk.membergrade;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-02 RecoveryTest — 회원 등급 10,000건 Chunk */
public final class EduBat02RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat02Handler(); }
}
