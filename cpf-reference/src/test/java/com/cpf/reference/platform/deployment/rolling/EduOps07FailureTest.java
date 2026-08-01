package com.cpf.reference.platform.deployment.rolling;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-07 FailureTest — Rolling 배포·Session·Connection Drain */
public final class EduOps07FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps07Handler(); }
}
