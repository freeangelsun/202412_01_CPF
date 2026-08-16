package com.cpf.education.scenarios.online.notification.export;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-13 IntegrationTest — 알림·비동기 내보내기·다운로드 감사 */
public final class EduDev13IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev13Handler(); }
}
