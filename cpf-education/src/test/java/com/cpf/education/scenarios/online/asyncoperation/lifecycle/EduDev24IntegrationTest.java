package com.cpf.education.scenarios.online.asyncoperation.lifecycle;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-24 IntegrationTest — 장시간 비동기 Operation 조회·취소 */
public final class EduDev24IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev24Handler(); }
}
