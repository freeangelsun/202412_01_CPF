package com.cpf.education.operations.gateway.servergroup;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-01 RecoveryTest — Server Group·Health·Load Balancing */
public final class EduGw01RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw01Handler(); }
}
