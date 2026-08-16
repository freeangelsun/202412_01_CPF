package com.cpf.education.scenarios.online.concurrency.optimisticlock;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-04 ConcurrencyTest — 동시 수정과 예상 Version 충돌 */
public final class EduDev04ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev04Handler(); }
}
