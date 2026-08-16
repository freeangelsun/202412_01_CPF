package com.cpf.education.operations.backoffice.evidence;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-06 RecoveryTest — 첨부·알림·감사·다운로드 */
public final class EduBackoffice06RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice06Handler(); }
}
