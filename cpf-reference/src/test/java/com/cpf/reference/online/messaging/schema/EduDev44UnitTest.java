package com.cpf.reference.online.messaging.schema;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-44 UnitTest — Event Schema 진화·호환성·Dead Letter */
public final class EduDev44UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev44Handler(); }
}
