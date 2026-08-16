package com.cpf.education.operations.gateway.route;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-02 UnitTest — Route·Predicate·Path Rewrite */
public final class EduGw02UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw02Handler(); }
}
