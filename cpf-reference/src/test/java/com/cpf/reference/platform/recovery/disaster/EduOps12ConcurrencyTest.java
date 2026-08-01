package com.cpf.reference.platform.recovery.disaster;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-12 ConcurrencyTest — 재해복구 전환·복귀·Split-Brain 방지 */
public final class EduOps12ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps12Handler(); }
}
