package com.cpf.reference.online.reference.effectiveperiod;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-19 RecoveryTest — 기준일·유효기간이 있는 기준정보 */
public final class EduDev19RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev19Handler(); }
}
