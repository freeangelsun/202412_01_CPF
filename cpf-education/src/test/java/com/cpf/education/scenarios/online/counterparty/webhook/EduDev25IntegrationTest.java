package com.cpf.education.scenarios.online.counterparty.webhook;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-25 IntegrationTest — Webhook Callback 서명·재전송·Replay 방지 */
public final class EduDev25IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev25Handler(); }
}
