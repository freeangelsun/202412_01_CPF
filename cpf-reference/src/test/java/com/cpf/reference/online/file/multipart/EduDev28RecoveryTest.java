package com.cpf.reference.online.file.multipart;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-28 RecoveryTest — 대용량 Multipart 업로드·중단 재개 */
public final class EduDev28RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev28Handler(); }
}
