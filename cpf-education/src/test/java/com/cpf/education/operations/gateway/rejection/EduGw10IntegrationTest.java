package com.cpf.education.operations.gateway.rejection;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-10 IntegrationTest — Body 크기·Content-Type·Schema Validation */
public final class EduGw10IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw10Handler(); }
}
