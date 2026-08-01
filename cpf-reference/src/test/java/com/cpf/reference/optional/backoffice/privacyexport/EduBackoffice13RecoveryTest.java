package com.cpf.reference.optional.backoffice.privacyexport;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-13 RecoveryTest — 개인정보 Masking·감사 조회·승인 Export */
public final class EduBackoffice13RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice13Handler(); }
}
