package com.cpf.education.operations.admin.notification;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-16 IntegrationTest — 알림 Acknowledge·Escalation·교대 인계 */
public final class EduAdm16IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm16Handler(); }
}
