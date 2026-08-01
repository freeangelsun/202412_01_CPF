package com.cpf.reference.batch.concurrency.execution;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-21 IntegrationTest — 중복 실행 방지·동시 실행 허용 범위 */
public final class EduBat21IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat21Handler(); }
}
