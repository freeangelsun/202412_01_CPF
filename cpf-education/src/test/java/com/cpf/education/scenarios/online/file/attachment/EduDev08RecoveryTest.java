package com.cpf.education.scenarios.online.file.attachment;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-08 RecoveryTest — 파일 업로드·검사·첨부·다운로드 */
public final class EduDev08RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev08Handler(); }
}
