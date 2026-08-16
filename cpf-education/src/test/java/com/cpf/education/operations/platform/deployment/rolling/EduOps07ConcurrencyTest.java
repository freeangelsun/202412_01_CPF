package com.cpf.education.operations.platform.deployment.rolling;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-07 ConcurrencyTest — Rolling 배포·Session·Connection Drain */
public final class EduOps07ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps07Handler(); }
}
