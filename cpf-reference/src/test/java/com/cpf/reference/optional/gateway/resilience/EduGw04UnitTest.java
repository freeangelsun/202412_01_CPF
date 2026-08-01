package com.cpf.reference.optional.gateway.resilience;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-04 UnitTest — Timeout·Retry·Circuit Breaker·Bulkhead */
public final class EduGw04UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw04Handler(); }
}
