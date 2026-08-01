package com.cpf.reference.batch.concurrency.execution;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-21 ConcurrencyTest — 중복 실행 방지·동시 실행 허용 범위 */
public final class EduBat21ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat21Handler(); }
}
