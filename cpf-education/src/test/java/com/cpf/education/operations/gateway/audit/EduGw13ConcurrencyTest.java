package com.cpf.education.operations.gateway.audit;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-13 ConcurrencyTest — Canary·가중치 Routing·Version Rollback */
public final class EduGw13ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw13Handler(); }
}
