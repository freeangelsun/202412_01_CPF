package com.cpf.education.scenarios.online.file.bulkimport;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-17 ConcurrencyTest — 대량 등록 사전검증·부분 오류 보고·재업로드 */
public final class EduDev17ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev17Handler(); }
}
