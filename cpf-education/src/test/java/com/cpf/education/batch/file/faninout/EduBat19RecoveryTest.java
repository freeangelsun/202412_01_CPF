package com.cpf.education.batch.file.faninout;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-19 RecoveryTest — 다중 파일 Fan-in·Fan-out */
public final class EduBat19RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat19Handler(); }
}
