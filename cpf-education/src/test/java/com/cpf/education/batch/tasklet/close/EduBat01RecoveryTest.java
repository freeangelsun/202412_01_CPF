package com.cpf.education.batch.tasklet.close;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-01 RecoveryTest — 업무일 마감 Tasklet */
public final class EduBat01RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat01Handler(); }
}
