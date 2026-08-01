package com.cpf.reference.batch.file.secureoutput;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-17 UnitTest — 암호화·압축·Checksum 파일 산출 */
public final class EduBat17UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat17Handler(); }
}
