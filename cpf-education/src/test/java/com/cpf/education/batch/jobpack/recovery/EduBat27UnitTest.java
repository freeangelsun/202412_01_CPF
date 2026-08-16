package com.cpf.education.batch.jobpack.recovery;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-27 UnitTest — Job Pack Checksum·호환성·이전 Version 복구 */
public final class EduBat27UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat27Handler(); }
}
