package com.cpf.reference.optional.backoffice.rollback;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-14 IntegrationTest — 고객 업무 승인 결과 반영·실패 Rollback */
public final class EduBackoffice14IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice14Handler(); }
}
