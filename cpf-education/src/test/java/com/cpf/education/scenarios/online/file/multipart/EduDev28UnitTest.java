package com.cpf.education.scenarios.online.file.multipart;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-28 UnitTest — 대용량 Multipart 업로드·중단 재개 */
public final class EduDev28UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev28Handler(); }
}
