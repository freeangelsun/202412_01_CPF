package com.cpf.reference.optional.gateway.resilience;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-04 FailureTest — Timeout·Retry·Circuit Breaker·Bulkhead */
public final class EduGw04FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw04Handler(); }
}
