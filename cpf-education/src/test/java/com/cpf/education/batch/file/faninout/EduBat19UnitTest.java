package com.cpf.education.batch.file.faninout;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-19 UnitTest — 다중 파일 Fan-in·Fan-out */
public final class EduBat19UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat19Handler(); }
}
