package com.cpf.education.operations.gateway.registry;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-07 ConcurrencyTest — Service Discovery·Target Failover·복귀 */
public final class EduGw07ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw07Handler(); }
}
