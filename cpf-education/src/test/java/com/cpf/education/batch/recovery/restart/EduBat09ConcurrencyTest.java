package com.cpf.education.batch.recovery.restart;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-09 ConcurrencyTest — 중지·재시작·실패건 재처리 */
public final class EduBat09ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat09Handler(); }
}
