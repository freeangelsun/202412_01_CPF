package com.cpf.education.operations.gateway.route;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-02 FailureTest — Route·Predicate·Path Rewrite */
public final class EduGw02FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw02Handler(); }
}
