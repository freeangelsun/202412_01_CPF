package com.cpf.reference.online.counterparty.rest;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-09 RecoveryTest — 외부 REST 신용조회와 결과 미확정 */
public final class EduDev09RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev09Handler(); }
}
