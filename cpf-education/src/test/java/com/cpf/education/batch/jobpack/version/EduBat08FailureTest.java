package com.cpf.education.batch.jobpack.version;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-08 FailureTest — Job Pack Version·Artifact 배포 */
public final class EduBat08FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat08Handler(); }
}
