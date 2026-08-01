package com.cpf.reference.optional.gateway.audit;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-13 UnitTest — Canary·가중치 Routing·Version Rollback */
public final class EduGw13UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw13Handler(); }
}
