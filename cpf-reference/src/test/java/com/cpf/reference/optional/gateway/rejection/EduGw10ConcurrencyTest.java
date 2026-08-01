package com.cpf.reference.optional.gateway.rejection;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-10 ConcurrencyTest — Body 크기·Content-Type·Schema Validation */
public final class EduGw10ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw10Handler(); }
}
