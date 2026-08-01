package com.cpf.reference.optional.gateway.route;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-02 RecoveryTest — Route·Predicate·Path Rewrite */
public final class EduGw02RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw02Handler(); }
}
