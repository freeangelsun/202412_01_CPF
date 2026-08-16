package com.cpf.education.batch.file.csv;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-03 ConcurrencyTest — CSV 입출력 배치 */
public final class EduBat03ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat03Handler(); }
}
