package com.cpf.education.batch.checkpoint.writercommit;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-13 IntegrationTest — Writer Commit 장애 후 Checkpoint 재시작 */
public final class EduBat13IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat13Handler(); }
}
