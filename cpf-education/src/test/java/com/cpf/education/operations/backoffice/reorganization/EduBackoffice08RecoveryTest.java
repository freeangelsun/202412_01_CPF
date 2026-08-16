package com.cpf.education.operations.backoffice.reorganization;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-08 RecoveryTest — 조직 개편·기준일·과거 이력 유지 */
public final class EduBackoffice08RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice08Handler(); }
}
