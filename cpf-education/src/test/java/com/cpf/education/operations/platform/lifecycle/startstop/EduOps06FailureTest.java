package com.cpf.education.operations.platform.lifecycle.startstop;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-06 FailureTest — 기동·종료·Health·Dependency 순서 */
public final class EduOps06FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps06Handler(); }
}
