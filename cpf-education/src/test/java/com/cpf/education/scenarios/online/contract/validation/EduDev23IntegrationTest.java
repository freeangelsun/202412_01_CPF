package com.cpf.education.scenarios.online.contract.validation;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-23 IntegrationTest — 공통 입력검증·오류 계약·OpenAPI 일치 */
public final class EduDev23IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev23Handler(); }
}
