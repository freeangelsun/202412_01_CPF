package com.cpf.education.scenarios.online.file.sftp;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-26 ConcurrencyTest — SFTP 수신·송신·완료 파일 원자 처리 */
public final class EduDev26ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev26Handler(); }
}
