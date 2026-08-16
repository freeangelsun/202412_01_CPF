package com.cpf.education.batch.backfill.latearrival;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-15 ConcurrencyTest — 지연 도착 데이터·Backfill·재산출 */
public final class EduBat15ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat15Handler(); }
}
