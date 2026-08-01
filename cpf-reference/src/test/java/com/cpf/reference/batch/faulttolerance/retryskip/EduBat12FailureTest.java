package com.cpf.reference.batch.faulttolerance.retryskip;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-12 FailureTest — Retry·Skip·No-Skip 예외 분류 */
public final class EduBat12FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat12Handler(); }
}
