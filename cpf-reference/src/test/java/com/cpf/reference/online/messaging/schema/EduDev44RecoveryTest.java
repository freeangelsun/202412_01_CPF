package com.cpf.reference.online.messaging.schema;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-44 RecoveryTest — Event Schema 진화·호환성·Dead Letter */
public final class EduDev44RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev44Handler(); }
}
