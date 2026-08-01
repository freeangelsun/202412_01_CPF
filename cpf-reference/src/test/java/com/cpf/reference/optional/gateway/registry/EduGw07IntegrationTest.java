package com.cpf.reference.optional.gateway.registry;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-07 IntegrationTest — Service Discovery·Target Failover·복귀 */
public final class EduGw07IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw07Handler(); }
}
