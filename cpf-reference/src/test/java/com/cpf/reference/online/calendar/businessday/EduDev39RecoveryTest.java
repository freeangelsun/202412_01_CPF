package com.cpf.reference.online.calendar.businessday;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-39 RecoveryTest — 업무일자·시간대·휴일 Calendar */
public final class EduDev39RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev39Handler(); }
}
