package com.cpf.reference.batch.jobpack.version;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-08 FailureTest — Job Pack Version·Artifact 배포 */
public final class EduBat08FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat08Handler(); }
}
