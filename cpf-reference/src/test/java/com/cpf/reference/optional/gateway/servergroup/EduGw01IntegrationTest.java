package com.cpf.reference.optional.gateway.servergroup;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-01 IntegrationTest — Server Group·Health·Load Balancing */
public final class EduGw01IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw01Handler(); }
}
