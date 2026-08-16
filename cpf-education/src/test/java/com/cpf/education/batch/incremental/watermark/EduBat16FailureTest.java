package com.cpf.education.batch.incremental.watermark;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-16 FailureTest — Watermark 기반 증분 수집·재시작 */
public final class EduBat16FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat16Handler(); }
}
