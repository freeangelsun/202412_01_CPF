package com.cpf.reference.optional.gateway.ratecontrol;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-12 FailureTest — 다중 인스턴스 설정 Drift·Reconcile */
public final class EduGw12FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw12Handler(); }
}
