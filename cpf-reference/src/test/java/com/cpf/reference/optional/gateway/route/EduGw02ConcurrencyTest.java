package com.cpf.reference.optional.gateway.route;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-02 ConcurrencyTest — Route·Predicate·Path Rewrite */
public final class EduGw02ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw02Handler(); }
}
