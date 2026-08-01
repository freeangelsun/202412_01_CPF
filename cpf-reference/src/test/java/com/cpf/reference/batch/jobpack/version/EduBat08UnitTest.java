package com.cpf.reference.batch.jobpack.version;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-08 UnitTest — Job Pack Version·Artifact 배포 */
public final class EduBat08UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat08Handler(); }
}
