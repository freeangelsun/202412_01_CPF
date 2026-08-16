package com.cpf.education.scenarios.online.notification.multichannel;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-31 IntegrationTest — 다중 채널 알림 선호·재시도·대체 채널 */
public final class EduDev31IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev31Handler(); }
}
