package com.cpf.education.operations.gateway.audit;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-13 FailureTest — Canary·가중치 Routing·Version Rollback */
public final class EduGw13FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw13Handler(); }
}
