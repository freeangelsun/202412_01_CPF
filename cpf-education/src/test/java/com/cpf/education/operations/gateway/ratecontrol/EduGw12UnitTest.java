package com.cpf.education.operations.gateway.ratecontrol;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-12 UnitTest — 다중 인스턴스 설정 Drift·Reconcile */
public final class EduGw12UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw12Handler(); }
}
