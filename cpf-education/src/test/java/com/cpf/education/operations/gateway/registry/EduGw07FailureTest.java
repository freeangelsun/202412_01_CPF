package com.cpf.education.operations.gateway.registry;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-07 FailureTest — Service Discovery·Target Failover·복귀 */
public final class EduGw07FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw07Handler(); }
}
