package com.cpf.reference.optional.gateway.rejection;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-10 UnitTest — Body 크기·Content-Type·Schema Validation */
public final class EduGw10UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw10Handler(); }
}
