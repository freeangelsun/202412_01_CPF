package com.cpf.education.scenarios.online.database.migration;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-14 RecoveryTest — Oracle·PostgreSQL·MariaDB 동일 의미 Migration */
public final class EduDev14RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev14Handler(); }
}
