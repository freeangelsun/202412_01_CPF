package com.cpf.reference.optional.gateway.servergroup;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-01 FailureTest — Server Group·Health·Load Balancing */
public final class EduGw01FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw01Handler(); }
}
