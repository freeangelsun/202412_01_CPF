package com.cpf.education.batch.checkpoint.writercommit;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-13 UnitTest — Writer Commit 장애 후 Checkpoint 재시작 */
public final class EduBat13UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat13Handler(); }
}
