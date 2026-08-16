package com.cpf.education.operations.platform.configuration.reconcile;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-09 ConcurrencyTest — 설정 변경 Partial Apply·Reconcile */
public final class EduOps09ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps09Handler(); }
}
