package com.cpf.reference.online.database.migration;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-14 RecoveryTest — Oracle·PostgreSQL·MariaDB 동일 의미 Migration */
public final class EduDev14RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev14Handler(); }
}
