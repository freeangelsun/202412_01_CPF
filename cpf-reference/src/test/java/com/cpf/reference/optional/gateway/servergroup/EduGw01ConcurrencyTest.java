package com.cpf.reference.optional.gateway.servergroup;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-01 ConcurrencyTest — Server Group·Health·Load Balancing */
public final class EduGw01ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw01Handler(); }
}
