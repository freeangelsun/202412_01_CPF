package com.cpf.education.operations.gateway.resilience;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-04 IntegrationTest — Timeout·Retry·Circuit Breaker·Bulkhead */
public final class EduGw04IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw04Handler(); }
}
