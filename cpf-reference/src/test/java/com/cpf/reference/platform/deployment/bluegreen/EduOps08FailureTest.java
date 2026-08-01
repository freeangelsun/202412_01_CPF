package com.cpf.reference.platform.deployment.bluegreen;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-08 FailureTest — Blue-Green·Canary 전환·되돌리기 */
public final class EduOps08FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps08Handler(); }
}
