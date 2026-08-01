package com.cpf.reference.online.counterparty.webhook;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-25 IntegrationTest — Webhook Callback 서명·재전송·Replay 방지 */
public final class EduDev25IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev25Handler(); }
}
