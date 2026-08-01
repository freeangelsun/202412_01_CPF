package com.cpf.reference.optional.gateway.rejection;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-10 FailureTest — Body 크기·Content-Type·Schema Validation */
public final class EduGw10FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw10Handler(); }
}
