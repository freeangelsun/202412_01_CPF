package com.cpf.education.operations.platform.upgrade.compatibility;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-15 RecoveryTest — Version Upgrade·DB 호환·Application Rollback */
public final class EduOps15RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps15Handler(); }
}
