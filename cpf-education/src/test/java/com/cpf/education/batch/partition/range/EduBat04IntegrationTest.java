package com.cpf.education.batch.partition.range;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-04 IntegrationTest — 8개 범위 Partition */
public final class EduBat04IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat04Handler(); }
}
