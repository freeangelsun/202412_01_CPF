package com.cpf.reference.online.calendar.businessday;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-39 FailureTest — 업무일자·시간대·휴일 Calendar */
public final class EduDev39FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev39Handler(); }
}
