package com.cpf.reference.online.database.migration;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-14 IntegrationTest — Oracle·PostgreSQL·MariaDB 동일 의미 Migration */
public final class EduDev14IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev14Handler(); }
}
