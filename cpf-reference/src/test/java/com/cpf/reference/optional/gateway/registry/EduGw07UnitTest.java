package com.cpf.reference.optional.gateway.registry;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-07 UnitTest — Service Discovery·Target Failover·복귀 */
public final class EduGw07UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw07Handler(); }
}
