package com.cpf.education.batch.faulttolerance.retryskip;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-12 UnitTest — Retry·Skip·No-Skip 예외 분류 */
public final class EduBat12UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat12Handler(); }
}
