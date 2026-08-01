package com.cpf.reference.platform.recovery.disaster;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-12 RecoveryTest — 재해복구 전환·복귀·Split-Brain 방지 */
public final class EduOps12RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps12Handler(); }
}
