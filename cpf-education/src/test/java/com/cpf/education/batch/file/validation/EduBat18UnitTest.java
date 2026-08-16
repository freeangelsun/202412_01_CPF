package com.cpf.education.batch.file.validation;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-18 UnitTest — 수신 파일 Header·Detail·Trailer 대사 */
public final class EduBat18UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat18Handler(); }
}
