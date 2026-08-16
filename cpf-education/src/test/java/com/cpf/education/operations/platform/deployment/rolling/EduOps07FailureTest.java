package com.cpf.education.operations.platform.deployment.rolling;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-07 FailureTest — Rolling 배포·Session·Connection Drain */
public final class EduOps07FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps07Handler(); }
}
