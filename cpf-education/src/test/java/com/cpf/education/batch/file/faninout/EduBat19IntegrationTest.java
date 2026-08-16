package com.cpf.education.batch.file.faninout;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-19 IntegrationTest — 다중 파일 Fan-in·Fan-out */
public final class EduBat19IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat19Handler(); }
}
