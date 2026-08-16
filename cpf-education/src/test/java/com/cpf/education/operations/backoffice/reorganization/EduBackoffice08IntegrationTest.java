package com.cpf.education.operations.backoffice.reorganization;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-08 IntegrationTest — 조직 개편·기준일·과거 이력 유지 */
public final class EduBackoffice08IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice08Handler(); }
}
