package com.cpf.reference.batch.faulttolerance.retryskip;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-12 UnitTest — Retry·Skip·No-Skip 예외 분류 */
public final class EduBat12UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat12Handler(); }
}
