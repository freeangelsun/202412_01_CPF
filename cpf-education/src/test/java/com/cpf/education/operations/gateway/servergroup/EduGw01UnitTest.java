package com.cpf.education.operations.gateway.servergroup;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-01 UnitTest — Server Group·Health·Load Balancing */
public final class EduGw01UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw01Handler(); }
}
