package com.cpf.reference.optional.gateway.route;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-02 IntegrationTest — Route·Predicate·Path Rewrite */
public final class EduGw02IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw02Handler(); }
}
