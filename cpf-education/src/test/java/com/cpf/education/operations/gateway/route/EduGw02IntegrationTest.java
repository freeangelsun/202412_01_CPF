package com.cpf.education.operations.gateway.route;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-02 IntegrationTest — Route·Predicate·Path Rewrite */
public final class EduGw02IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw02Handler(); }
}
