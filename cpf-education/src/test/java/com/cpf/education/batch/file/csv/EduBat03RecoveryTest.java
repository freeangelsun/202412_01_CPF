package com.cpf.education.batch.file.csv;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-03 RecoveryTest — CSV 입출력 배치 */
public final class EduBat03RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat03Handler(); }
}
