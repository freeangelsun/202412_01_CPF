package com.cpf.education.scenarios.online.money.exchange;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-40 RecoveryTest — 금액·통화·반올림·환율 Version */
public final class EduDev40RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev40Handler(); }
}
