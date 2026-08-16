package com.cpf.education.operations.platform.database.lifecycle;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-04 IntegrationTest — DB 3종 신규 설치·Migration·Drift·Rollback */
public final class EduOps04IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps04Handler(); }
}
