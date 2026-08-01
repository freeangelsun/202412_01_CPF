package com.cpf.reference.batch.file.faninout;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-19 IntegrationTest — 다중 파일 Fan-in·Fan-out */
public final class EduBat19IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat19Handler(); }
}
