package com.cpf.reference.batch.performance.backpressure;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-30 ConcurrencyTest — 대용량 처리 성능·용량·Backpressure */
public final class EduBat30ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat30Handler(); }
}
