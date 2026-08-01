package com.cpf.reference.platform.recovery.disaster;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-12 IntegrationTest — 재해복구 전환·복귀·Split-Brain 방지 */
public final class EduOps12IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps12Handler(); }
}
