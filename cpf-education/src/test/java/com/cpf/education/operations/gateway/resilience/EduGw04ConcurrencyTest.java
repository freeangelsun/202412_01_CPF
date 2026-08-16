package com.cpf.education.operations.gateway.resilience;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-04 ConcurrencyTest — Timeout·Retry·Circuit Breaker·Bulkhead */
public final class EduGw04ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw04Handler(); }
}
