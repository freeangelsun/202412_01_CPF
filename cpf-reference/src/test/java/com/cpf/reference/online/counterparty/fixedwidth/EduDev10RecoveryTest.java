package com.cpf.reference.online.counterparty.fixedwidth;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-10 RecoveryTest — 고정길이 전문 기관 이체 */
public final class EduDev10RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev10Handler(); }
}
