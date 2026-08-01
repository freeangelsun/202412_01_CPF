package com.cpf.reference.platform.configuration.reconcile;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-09 RecoveryTest — 설정 변경 Partial Apply·Reconcile */
public final class EduOps09RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps09Handler(); }
}
