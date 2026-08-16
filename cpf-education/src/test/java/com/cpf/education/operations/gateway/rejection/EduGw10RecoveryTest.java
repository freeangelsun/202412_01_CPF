package com.cpf.education.operations.gateway.rejection;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-10 RecoveryTest — Body 크기·Content-Type·Schema Validation */
public final class EduGw10RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw10Handler(); }
}
