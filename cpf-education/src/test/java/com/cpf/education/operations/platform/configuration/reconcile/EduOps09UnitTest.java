package com.cpf.education.operations.platform.configuration.reconcile;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-09 UnitTest — 설정 변경 Partial Apply·Reconcile */
public final class EduOps09UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps09Handler(); }
}
