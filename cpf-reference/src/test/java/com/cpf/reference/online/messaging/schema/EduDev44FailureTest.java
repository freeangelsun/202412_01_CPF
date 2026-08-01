package com.cpf.reference.online.messaging.schema;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-44 FailureTest — Event Schema 진화·호환성·Dead Letter */
public final class EduDev44FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev44Handler(); }
}
