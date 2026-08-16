package com.cpf.education.operations.platform.deployment.bluegreen;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-08 UnitTest — Blue-Green·Canary 전환·되돌리기 */
public final class EduOps08UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps08Handler(); }
}
