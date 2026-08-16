package com.cpf.education.operations.platform.deployment.bluegreen;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-08 FailureTest — Blue-Green·Canary 전환·되돌리기 */
public final class EduOps08FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps08Handler(); }
}
