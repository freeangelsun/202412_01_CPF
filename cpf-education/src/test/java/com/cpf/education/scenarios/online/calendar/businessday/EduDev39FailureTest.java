package com.cpf.education.scenarios.online.calendar.businessday;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-39 FailureTest — 업무일자·시간대·휴일 Calendar */
public final class EduDev39FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev39Handler(); }
}
