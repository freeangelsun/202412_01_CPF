package com.cpf.education.scenarios.online.file.bulkimport;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-17 RecoveryTest — 대량 등록 사전검증·부분 오류 보고·재업로드 */
public final class EduDev17RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev17Handler(); }
}
