package com.cpf.reference.online.notification.multichannel;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-31 IntegrationTest — 다중 채널 알림 선호·재시도·대체 채널 */
public final class EduDev31IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev31Handler(); }
}
