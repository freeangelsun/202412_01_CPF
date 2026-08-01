package com.cpf.reference.optional.gateway.rejection;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-10 RecoveryTest — Body 크기·Content-Type·Schema Validation */
public final class EduGw10RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw10Handler(); }
}
