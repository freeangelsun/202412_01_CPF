package com.cpf.education.batch.file.csv;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-03 FailureTest — CSV 입출력 배치 */
public final class EduBat03FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat03Handler(); }
}
