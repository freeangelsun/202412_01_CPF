package com.cpf.reference.batch.chunk.membergrade;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-02 IntegrationTest — 회원 등급 10,000건 Chunk */
public final class EduBat02IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat02Handler(); }
}
