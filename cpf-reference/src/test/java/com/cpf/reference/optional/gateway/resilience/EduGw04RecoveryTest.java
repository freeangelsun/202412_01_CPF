package com.cpf.reference.optional.gateway.resilience;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-04 RecoveryTest — Timeout·Retry·Circuit Breaker·Bulkhead */
public final class EduGw04RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw04Handler(); }
}
