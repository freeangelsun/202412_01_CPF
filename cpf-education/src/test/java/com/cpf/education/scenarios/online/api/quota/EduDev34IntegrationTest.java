package com.cpf.education.scenarios.online.api.quota;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-34 IntegrationTest — API 사용량 제한·고객별 Quota·초과 처리 */
public final class EduDev34IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev34Handler(); }
}
