package com.cpf.reference.online.calendar.businessday;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-39 UnitTest — 업무일자·시간대·휴일 Calendar */
public final class EduDev39UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev39Handler(); }
}
