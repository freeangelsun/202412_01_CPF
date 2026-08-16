package com.cpf.education.scenarios.online.idempotency.payment;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-05 UnitTest — 지급 등록 멱등성·응답 유실·결과 대사 */
public final class EduDev05UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev05Handler(); }
}
