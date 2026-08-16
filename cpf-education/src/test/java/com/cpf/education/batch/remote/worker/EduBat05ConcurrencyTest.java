package com.cpf.education.batch.remote.worker;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-05 ConcurrencyTest — Manager·Worker·Lease·Fencing */
public final class EduBat05ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat05Handler(); }
}
