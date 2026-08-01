package com.cpf.reference.batch.remote.worker;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-05 RecoveryTest — Manager·Worker·Lease·Fencing */
public final class EduBat05RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat05Handler(); }
}
