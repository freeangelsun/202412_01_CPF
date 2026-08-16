package com.cpf.education.operations.platform.recovery.disaster;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-12 UnitTest — 재해복구 전환·복귀·Split-Brain 방지 */
public final class EduOps12UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps12Handler(); }
}
