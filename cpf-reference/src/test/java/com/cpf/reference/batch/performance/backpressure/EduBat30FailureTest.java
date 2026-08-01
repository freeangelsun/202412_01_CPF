package com.cpf.reference.batch.performance.backpressure;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-30 FailureTest — 대용량 처리 성능·용량·Backpressure */
public final class EduBat30FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat30Handler(); }
}
