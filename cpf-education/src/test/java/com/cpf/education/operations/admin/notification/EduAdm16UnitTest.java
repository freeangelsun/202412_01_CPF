package com.cpf.education.operations.admin.notification;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-16 UnitTest — 알림 Acknowledge·Escalation·교대 인계 */
public final class EduAdm16UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm16Handler(); }
}
