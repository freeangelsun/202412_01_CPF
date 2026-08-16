package com.cpf.education.batch.jobpack.recovery;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-27 ConcurrencyTest — Job Pack Checksum·호환성·이전 Version 복구 */
public final class EduBat27ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat27Handler(); }
}
