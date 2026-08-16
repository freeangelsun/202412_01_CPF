package com.cpf.education.operations.platform.database.lifecycle;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-04 ConcurrencyTest — DB 3종 신규 설치·Migration·Drift·Rollback */
public final class EduOps04ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps04Handler(); }
}
