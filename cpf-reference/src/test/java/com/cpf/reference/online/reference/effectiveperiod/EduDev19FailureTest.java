package com.cpf.reference.online.reference.effectiveperiod;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-19 FailureTest — 기준일·유효기간이 있는 기준정보 */
public final class EduDev19FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev19Handler(); }
}
