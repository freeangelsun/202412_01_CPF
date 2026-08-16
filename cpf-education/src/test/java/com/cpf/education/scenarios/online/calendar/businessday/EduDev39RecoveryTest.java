package com.cpf.education.scenarios.online.calendar.businessday;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-39 RecoveryTest — 업무일자·시간대·휴일 Calendar */
public final class EduDev39RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev39Handler(); }
}
