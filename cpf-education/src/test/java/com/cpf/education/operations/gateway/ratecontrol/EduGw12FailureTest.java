package com.cpf.education.operations.gateway.ratecontrol;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-12 FailureTest — 다중 인스턴스 설정 Drift·Reconcile */
public final class EduGw12FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw12Handler(); }
}
