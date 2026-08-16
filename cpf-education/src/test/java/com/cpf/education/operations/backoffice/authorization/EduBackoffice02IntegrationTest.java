package com.cpf.education.operations.backoffice.authorization;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-02 IntegrationTest — 사용자·역할·권한·실효 권한 */
public final class EduBackoffice02IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice02Handler(); }
}
