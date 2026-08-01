package com.cpf.reference.online.calendar.businessday;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-39 ConcurrencyTest — 업무일자·시간대·휴일 Calendar */
public final class EduDev39ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev39Handler(); }
}
