package com.cpf.reference.platform.configuration.reconcile;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-09 FailureTest — 설정 변경 Partial Apply·Reconcile */
public final class EduOps09FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps09Handler(); }
}
