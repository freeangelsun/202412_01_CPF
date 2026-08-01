package com.cpf.reference.batch.incremental.watermark;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-16 ConcurrencyTest — Watermark 기반 증분 수집·재시작 */
public final class EduBat16ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat16Handler(); }
}
