package com.cpf.education.operations.gateway.resilience;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-04 RecoveryTest — Timeout·Retry·Circuit Breaker·Bulkhead */
public final class EduGw04RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw04Handler(); }
}
