package com.cpf.reference.batch.file.faninout;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-19 FailureTest — 다중 파일 Fan-in·Fan-out */
public final class EduBat19FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat19Handler(); }
}
