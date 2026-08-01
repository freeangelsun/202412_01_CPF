package com.cpf.reference.batch.file.validation;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-18 FailureTest — 수신 파일 Header·Detail·Trailer 대사 */
public final class EduBat18FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat18Handler(); }
}
