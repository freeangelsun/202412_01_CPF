package com.cpf.education.scenarios.online.api.versioning;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-43 RecoveryTest — API Version 전환·하위 호환·폐기 */
public final class EduDev43RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev43Handler(); }
}
