package com.cpf.education.scenarios.online.messaging.schema;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-44 IntegrationTest — Event Schema 진화·호환성·Dead Letter */
public final class EduDev44IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev44Handler(); }
}
