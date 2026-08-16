package com.cpf.education.batch.backfill.latearrival;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-15 UnitTest — 지연 도착 데이터·Backfill·재산출 */
public final class EduBat15UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat15Handler(); }
}
