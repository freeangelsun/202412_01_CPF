package com.cpf.education.operations.gateway.route;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-02 RecoveryTest — Route·Predicate·Path Rewrite */
public final class EduGw02RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw02Handler(); }
}
