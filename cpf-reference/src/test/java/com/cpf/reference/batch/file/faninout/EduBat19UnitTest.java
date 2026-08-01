package com.cpf.reference.batch.file.faninout;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-19 UnitTest — 다중 파일 Fan-in·Fan-out */
public final class EduBat19UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat19Handler(); }
}
