package com.cpf.education.batch.centercut.reconcile;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-26 IntegrationTest — 센터컷 결과 대사·차이 보정·재실행 */
public final class EduBat26IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat26Handler(); }
}
