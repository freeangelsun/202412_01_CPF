package com.cpf.reference.batch.remote.worker;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-05 UnitTest — Manager·Worker·Lease·Fencing */
public final class EduBat05UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat05Handler(); }
}
