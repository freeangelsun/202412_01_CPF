package com.cpf.education.operations.backoffice.delegation;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-05 IntegrationTest — 위임·대결·대행 책임 */
public final class EduBackoffice05IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice05Handler(); }
}
