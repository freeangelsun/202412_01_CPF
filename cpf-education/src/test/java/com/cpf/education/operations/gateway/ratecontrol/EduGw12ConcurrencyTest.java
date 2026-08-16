package com.cpf.education.operations.gateway.ratecontrol;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-12 ConcurrencyTest — 다중 인스턴스 설정 Drift·Reconcile */
public final class EduGw12ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw12Handler(); }
}
