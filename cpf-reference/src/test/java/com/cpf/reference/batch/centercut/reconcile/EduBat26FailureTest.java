package com.cpf.reference.batch.centercut.reconcile;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-26 FailureTest — 센터컷 결과 대사·차이 보정·재실행 */
public final class EduBat26FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat26Handler(); }
}
