package com.cpf.reference.optional.gateway.audit;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-13 IntegrationTest — Canary·가중치 Routing·Version Rollback */
public final class EduGw13IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw13Handler(); }
}
