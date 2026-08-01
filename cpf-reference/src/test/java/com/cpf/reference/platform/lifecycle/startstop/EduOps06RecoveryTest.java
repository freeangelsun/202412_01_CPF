package com.cpf.reference.platform.lifecycle.startstop;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-06 RecoveryTest — 기동·종료·Health·Dependency 순서 */
public final class EduOps06RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps06Handler(); }
}
