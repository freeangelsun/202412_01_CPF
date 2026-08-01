package com.cpf.reference.optional.gateway.route;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-02 UnitTest — Route·Predicate·Path Rewrite */
public final class EduGw02UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw02Handler(); }
}
