package com.cpf.reference.online.file.sftp;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-26 RecoveryTest — SFTP 수신·송신·완료 파일 원자 처리 */
public final class EduDev26RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev26Handler(); }
}
