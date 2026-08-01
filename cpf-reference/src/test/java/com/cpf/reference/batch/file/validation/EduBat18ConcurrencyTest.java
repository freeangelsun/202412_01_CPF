package com.cpf.reference.batch.file.validation;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-18 ConcurrencyTest — 수신 파일 Header·Detail·Trailer 대사 */
public final class EduBat18ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat18Handler(); }
}
