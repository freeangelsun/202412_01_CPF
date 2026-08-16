package com.cpf.education.operations.gateway.resilience;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-04 UnitTest — Timeout·Retry·Circuit Breaker·Bulkhead */
public final class EduGw04UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw04Handler(); }
}
