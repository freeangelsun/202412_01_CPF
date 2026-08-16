package com.cpf.education.scenarios.online.counterparty.soap;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-27 RecoveryTest — SOAP·XML 외부기관 연계와 Fault 처리 */
public final class EduDev27RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev27Handler(); }
}
