package com.cpf.education.scenarios.online.file.multipart;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-28 FailureTest — 대용량 Multipart 업로드·중단 재개 */
public final class EduDev28FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev28Handler(); }
}
