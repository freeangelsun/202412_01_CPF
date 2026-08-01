package com.cpf.reference.optional.gateway.resilience;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-04 ConcurrencyTest — Timeout·Retry·Circuit Breaker·Bulkhead */
public final class EduGw04ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw04Handler(); }
}
