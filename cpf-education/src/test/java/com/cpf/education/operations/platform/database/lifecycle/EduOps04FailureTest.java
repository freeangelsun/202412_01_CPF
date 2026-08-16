package com.cpf.education.operations.platform.database.lifecycle;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-04 FailureTest — DB 3종 신규 설치·Migration·Drift·Rollback */
public final class EduOps04FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps04Handler(); }
}
