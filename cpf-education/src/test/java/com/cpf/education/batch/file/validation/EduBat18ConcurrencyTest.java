package com.cpf.education.batch.file.validation;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-18 ConcurrencyTest — 수신 파일 Header·Detail·Trailer 대사 */
public final class EduBat18ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat18Handler(); }
}
