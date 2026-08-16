package com.cpf.education.batch.incremental.watermark;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-16 RecoveryTest — Watermark 기반 증분 수집·재시작 */
public final class EduBat16RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat16Handler(); }
}
