package com.cpf.education.batch.file.secureoutput;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-17 IntegrationTest — 암호화·압축·Checksum 파일 산출 */
public final class EduBat17IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat17Handler(); }
}
