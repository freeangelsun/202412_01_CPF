package com.cpf.education.batch.remote.worker;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-05 FailureTest — Manager·Worker·Lease·Fencing */
public final class EduBat05FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat05Handler(); }
}
