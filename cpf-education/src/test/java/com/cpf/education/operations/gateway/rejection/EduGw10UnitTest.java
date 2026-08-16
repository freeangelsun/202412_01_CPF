package com.cpf.education.operations.gateway.rejection;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-10 UnitTest — Body 크기·Content-Type·Schema Validation */
public final class EduGw10UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw10Handler(); }
}
