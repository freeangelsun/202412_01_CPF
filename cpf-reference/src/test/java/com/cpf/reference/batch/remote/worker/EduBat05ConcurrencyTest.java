package com.cpf.reference.batch.remote.worker;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-05 ConcurrencyTest — Manager·Worker·Lease·Fencing */
public final class EduBat05ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat05Handler(); }
}
