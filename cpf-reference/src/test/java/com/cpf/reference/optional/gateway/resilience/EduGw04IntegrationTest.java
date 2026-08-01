package com.cpf.reference.optional.gateway.resilience;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-04 IntegrationTest — Timeout·Retry·Circuit Breaker·Bulkhead */
public final class EduGw04IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw04Handler(); }
}
