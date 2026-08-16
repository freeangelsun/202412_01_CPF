package com.cpf.education.batch.jobpack.version;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-08 RecoveryTest — Job Pack Version·Artifact 배포 */
public final class EduBat08RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat08Handler(); }
}
