package com.cpf.education.batch.lifecycle.stopabandon;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-23 RecoveryTest — Stop·Abandon·Restart 의미 분리 */
public final class EduBat23RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat23Handler(); }
}
