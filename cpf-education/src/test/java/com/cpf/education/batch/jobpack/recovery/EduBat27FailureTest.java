package com.cpf.education.batch.jobpack.recovery;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-27 FailureTest — Job Pack Checksum·호환성·이전 Version 복구 */
public final class EduBat27FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat27Handler(); }
}
