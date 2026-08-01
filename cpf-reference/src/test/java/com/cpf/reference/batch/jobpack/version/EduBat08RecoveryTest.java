package com.cpf.reference.batch.jobpack.version;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-08 RecoveryTest — Job Pack Version·Artifact 배포 */
public final class EduBat08RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat08Handler(); }
}
