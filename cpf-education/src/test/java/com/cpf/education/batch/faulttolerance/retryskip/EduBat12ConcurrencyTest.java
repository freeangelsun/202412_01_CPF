package com.cpf.education.batch.faulttolerance.retryskip;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-12 ConcurrencyTest — Retry·Skip·No-Skip 예외 분류 */
public final class EduBat12ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat12Handler(); }
}
