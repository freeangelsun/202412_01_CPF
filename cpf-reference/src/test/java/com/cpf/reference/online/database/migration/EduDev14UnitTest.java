package com.cpf.reference.online.database.migration;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-14 UnitTest — Oracle·PostgreSQL·MariaDB 동일 의미 Migration */
public final class EduDev14UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev14Handler(); }
}
