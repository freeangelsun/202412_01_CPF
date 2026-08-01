package com.cpf.reference.optional.gateway.audit;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-13 ConcurrencyTest — Canary·가중치 Routing·Version Rollback */
public final class EduGw13ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw13Handler(); }
}
