package com.cpf.reference.online.counterparty.webhook;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-25 RecoveryTest — Webhook Callback 서명·재전송·Replay 방지 */
public final class EduDev25RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev25Handler(); }
}
