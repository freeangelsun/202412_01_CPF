package com.cpf.education.operations.gateway.servergroup;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-01 ConcurrencyTest — Server Group·Health·Load Balancing */
public final class EduGw01ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw01Handler(); }
}
