package com.cpf.education.batch.file.validation;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-18 RecoveryTest — 수신 파일 Header·Detail·Trailer 대사 */
public final class EduBat18RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat18Handler(); }
}
