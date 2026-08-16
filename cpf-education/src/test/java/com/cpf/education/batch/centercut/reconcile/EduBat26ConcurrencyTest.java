package com.cpf.education.batch.centercut.reconcile;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-26 ConcurrencyTest — 센터컷 결과 대사·차이 보정·재실행 */
public final class EduBat26ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat26Handler(); }
}
