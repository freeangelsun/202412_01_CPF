package com.cpf.education.operations.gateway.rejection;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-10 FailureTest — Body 크기·Content-Type·Schema Validation */
public final class EduGw10FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw10Handler(); }
}
