package com.cpf.reference.optional.gateway.servergroup;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-01 RecoveryTest — Server Group·Health·Load Balancing */
public final class EduGw01RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw01Handler(); }
}
