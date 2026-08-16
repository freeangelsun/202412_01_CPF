package com.cpf.education.batch.file.csv;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-03 IntegrationTest — CSV 입출력 배치 */
public final class EduBat03IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat03Handler(); }
}
