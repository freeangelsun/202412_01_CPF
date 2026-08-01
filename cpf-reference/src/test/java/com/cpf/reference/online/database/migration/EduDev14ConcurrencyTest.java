package com.cpf.reference.online.database.migration;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-14 ConcurrencyTest — Oracle·PostgreSQL·MariaDB 동일 의미 Migration */
public final class EduDev14ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev14Handler(); }
}
