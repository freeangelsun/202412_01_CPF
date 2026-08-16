package com.cpf.education.operations.gateway.servergroup;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-01 FailureTest — Server Group·Health·Load Balancing */
public final class EduGw01FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw01Handler(); }
}
