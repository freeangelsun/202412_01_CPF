package com.cpf.reference.optional.gateway.registry;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-07 FailureTest — Service Discovery·Target Failover·복귀 */
public final class EduGw07FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw07Handler(); }
}
