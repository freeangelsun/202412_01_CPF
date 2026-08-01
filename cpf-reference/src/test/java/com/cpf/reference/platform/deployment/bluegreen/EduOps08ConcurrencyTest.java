package com.cpf.reference.platform.deployment.bluegreen;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-08 ConcurrencyTest — Blue-Green·Canary 전환·되돌리기 */
public final class EduOps08ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps08Handler(); }
}
