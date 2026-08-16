package com.cpf.education.operations.gateway.registry;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-07 UnitTest — Service Discovery·Target Failover·복귀 */
public final class EduGw07UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw07Handler(); }
}
