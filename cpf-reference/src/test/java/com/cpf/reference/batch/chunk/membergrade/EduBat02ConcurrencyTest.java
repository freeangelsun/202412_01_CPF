package com.cpf.reference.batch.chunk.membergrade;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-02 ConcurrencyTest — 회원 등급 10,000건 Chunk */
public final class EduBat02ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat02Handler(); }
}
