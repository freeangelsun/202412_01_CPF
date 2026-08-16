package com.cpf.education.batch.tasklet.close;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-01 FailureTest — 업무일 마감 Tasklet */
public final class EduBat01FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat01Handler(); }
}
