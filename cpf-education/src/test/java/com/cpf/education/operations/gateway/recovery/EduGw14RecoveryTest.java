package com.cpf.education.operations.gateway.recovery;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-14 RecoveryTest — Gateway 관측·개인정보 가림·감사 */
public final class EduGw14RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw14Handler(); }
}
