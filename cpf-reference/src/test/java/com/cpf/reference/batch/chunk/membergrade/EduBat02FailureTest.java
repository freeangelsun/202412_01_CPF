package com.cpf.reference.batch.chunk.membergrade;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-02 FailureTest — 회원 등급 10,000건 Chunk */
public final class EduBat02FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat02Handler(); }
}
