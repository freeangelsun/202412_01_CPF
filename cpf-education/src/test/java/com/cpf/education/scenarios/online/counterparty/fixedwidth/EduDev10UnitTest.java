package com.cpf.education.scenarios.online.counterparty.fixedwidth;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-10 UnitTest — 고정길이 전문 기관 이체 */
public final class EduDev10UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev10Handler(); }
}
