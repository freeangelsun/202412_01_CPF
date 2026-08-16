package com.cpf.education.batch.centercut.approval;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-06 FailureTest — 센터컷 Preview·승인·실행 */
public final class EduBat06FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat06Handler(); }
}
