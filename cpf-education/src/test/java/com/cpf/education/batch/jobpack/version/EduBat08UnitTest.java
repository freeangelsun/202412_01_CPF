package com.cpf.education.batch.jobpack.version;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-08 UnitTest — Job Pack Version·Artifact 배포 */
public final class EduBat08UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat08Handler(); }
}
