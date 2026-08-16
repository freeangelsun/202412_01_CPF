package com.cpf.education.batch.file.secureoutput;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-17 FailureTest — 암호화·압축·Checksum 파일 산출 */
public final class EduBat17FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat17Handler(); }
}
