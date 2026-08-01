package com.cpf.reference.optional.backoffice.reorganization;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-08 IntegrationTest — 조직 개편·기준일·과거 이력 유지 */
public final class EduBackoffice08IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice08Handler(); }
}
