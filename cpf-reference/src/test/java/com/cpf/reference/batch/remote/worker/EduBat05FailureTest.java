package com.cpf.reference.batch.remote.worker;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-05 FailureTest — Manager·Worker·Lease·Fencing */
public final class EduBat05FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat05Handler(); }
}
