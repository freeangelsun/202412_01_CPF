package com.cpf.reference.online.messaging.schema;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-44 ConcurrencyTest — Event Schema 진화·호환성·Dead Letter */
public final class EduDev44ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev44Handler(); }
}
