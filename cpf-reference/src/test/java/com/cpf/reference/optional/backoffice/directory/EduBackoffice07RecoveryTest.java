package com.cpf.reference.optional.backoffice.directory;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-07 RecoveryTest — 초기 관리자 Bootstrap·첫 로그인·권한 인계 */
public final class EduBackoffice07RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice07Handler(); }
}
