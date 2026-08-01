package com.cpf.reference.batch.instance.parameter;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-14 IntegrationTest — JobParameter 식별·중복 실행·새 Instance */
public final class EduBat14IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat14Handler(); }
}
