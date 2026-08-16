package com.cpf.education.scenarios.online.masterdata.effectiveperiod;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-19 ConcurrencyTest — 기준일·유효기간이 있는 기준정보 */
public final class EduDev19ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev19Handler(); }
}
