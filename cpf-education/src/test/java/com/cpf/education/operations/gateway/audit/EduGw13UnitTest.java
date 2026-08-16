package com.cpf.education.operations.gateway.audit;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-13 UnitTest — Canary·가중치 Routing·Version Rollback */
public final class EduGw13UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw13Handler(); }
}
