package com.cpf.education.batch.remote.worker;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-05 UnitTest — Manager·Worker·Lease·Fencing */
public final class EduBat05UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat05Handler(); }
}
