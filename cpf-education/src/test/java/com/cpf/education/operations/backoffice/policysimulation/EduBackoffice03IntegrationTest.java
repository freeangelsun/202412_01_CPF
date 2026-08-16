package com.cpf.education.operations.backoffice.policysimulation;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-03 IntegrationTest — 결재정책 Version·경로 사전 계산 */
public final class EduBackoffice03IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice03Handler(); }
}
