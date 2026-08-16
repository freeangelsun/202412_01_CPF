package com.cpf.education.batch.file.csv;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-03 UnitTest — CSV 입출력 배치 */
public final class EduBat03UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat03Handler(); }
}
