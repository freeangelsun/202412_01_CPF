package com.cpf.education.operations.admin.notification;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-16 RecoveryTest — 알림 Acknowledge·Escalation·교대 인계 */
public final class EduAdm16RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm16Handler(); }
}
