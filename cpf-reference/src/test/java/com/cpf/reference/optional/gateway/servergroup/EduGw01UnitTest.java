package com.cpf.reference.optional.gateway.servergroup;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-01 UnitTest — Server Group·Health·Load Balancing */
public final class EduGw01UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw01Handler(); }
}
