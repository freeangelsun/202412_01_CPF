package com.cpf.education.batch.file.faninout;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-19 ConcurrencyTest — 다중 파일 Fan-in·Fan-out */
public final class EduBat19ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat19Handler(); }
}
