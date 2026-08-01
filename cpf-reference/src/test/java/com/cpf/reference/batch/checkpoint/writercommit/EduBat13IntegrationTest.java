package com.cpf.reference.batch.checkpoint.writercommit;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-13 IntegrationTest — Writer Commit 장애 후 Checkpoint 재시작 */
public final class EduBat13IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat13Handler(); }
}
