package com.cpf.education.scenarios.online.query.searchindex;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-45 IntegrationTest — 조회 모델·검색색인 Eventual Consistency */
public final class EduDev45IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev45Handler(); }
}
