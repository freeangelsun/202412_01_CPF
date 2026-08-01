package com.cpf.reference.online.messaging.schema;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-44 IntegrationTest — Event Schema 진화·호환성·Dead Letter */
public final class EduDev44IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev44Handler(); }
}
