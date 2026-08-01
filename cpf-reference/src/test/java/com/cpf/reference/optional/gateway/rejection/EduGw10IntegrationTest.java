package com.cpf.reference.optional.gateway.rejection;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-10 IntegrationTest — Body 크기·Content-Type·Schema Validation */
public final class EduGw10IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw10Handler(); }
}
