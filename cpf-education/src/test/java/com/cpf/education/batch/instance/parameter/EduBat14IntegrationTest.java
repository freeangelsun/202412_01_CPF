package com.cpf.education.batch.instance.parameter;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-14 IntegrationTest — JobParameter 식별·중복 실행·새 Instance */
public final class EduBat14IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat14Handler(); }
}
