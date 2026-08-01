package com.cpf.reference.platform.deployment.bluegreen;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-08 UnitTest — Blue-Green·Canary 전환·되돌리기 */
public final class EduOps08UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps08Handler(); }
}
