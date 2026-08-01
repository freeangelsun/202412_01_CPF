package com.cpf.reference.batch.remote.reassignment;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-24 RecoveryTest — Remote Worker 유실·재할당·중복 결과 차단 */
public final class EduBat24RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat24Handler(); }
}
