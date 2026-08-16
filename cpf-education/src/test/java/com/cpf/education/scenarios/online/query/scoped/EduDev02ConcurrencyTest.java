package com.cpf.education.scenarios.online.query.scoped;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-02 ConcurrencyTest — 권한·범위가 적용된 목록·상세 조회 */
public final class EduDev02ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev02Handler(); }
}
