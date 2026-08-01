package com.cpf.reference.platform.recovery.disaster;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-12 FailureTest — 재해복구 전환·복귀·Split-Brain 방지 */
public final class EduOps12FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps12Handler(); }
}
