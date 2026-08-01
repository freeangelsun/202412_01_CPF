package com.cpf.reference.optional.gateway.route;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-02 FailureTest — Route·Predicate·Path Rewrite */
public final class EduGw02FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw02Handler(); }
}
