package com.cpf.reference.batch.file.validation;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-18 IntegrationTest — 수신 파일 Header·Detail·Trailer 대사 */
public final class EduBat18IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat18Handler(); }
}
