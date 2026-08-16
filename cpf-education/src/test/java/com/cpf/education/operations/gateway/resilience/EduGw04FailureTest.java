package com.cpf.education.operations.gateway.resilience;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-04 FailureTest — Timeout·Retry·Circuit Breaker·Bulkhead */
public final class EduGw04FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw04Handler(); }
}
