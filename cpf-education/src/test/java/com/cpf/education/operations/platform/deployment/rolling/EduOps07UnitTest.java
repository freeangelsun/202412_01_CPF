package com.cpf.education.operations.platform.deployment.rolling;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-07 UnitTest — Rolling 배포·Session·Connection Drain */
public final class EduOps07UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps07Handler(); }
}
