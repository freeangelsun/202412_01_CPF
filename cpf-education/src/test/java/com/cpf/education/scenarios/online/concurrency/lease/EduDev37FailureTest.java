package com.cpf.education.scenarios.online.concurrency.lease;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-37 FailureTest — 온라인 분산 Lease·Fencing·소유권 상실 */
public final class EduDev37FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev37Handler(); }
}
