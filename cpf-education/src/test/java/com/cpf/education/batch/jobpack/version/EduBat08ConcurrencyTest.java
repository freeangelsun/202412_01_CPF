package com.cpf.education.batch.jobpack.version;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-08 ConcurrencyTest — Job Pack Version·Artifact 배포 */
public final class EduBat08ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat08Handler(); }
}
