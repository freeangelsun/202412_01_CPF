package com.cpf.education.scenarios.online.counterparty.rest;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-09 ConcurrencyTest — 외부 REST 신용조회와 결과 미확정 */
public final class EduDev09ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev09Handler(); }
}
