package com.cpf.education.batch.remote.worker;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-05 RecoveryTest — Manager·Worker·Lease·Fencing */
public final class EduBat05RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat05Handler(); }
}
