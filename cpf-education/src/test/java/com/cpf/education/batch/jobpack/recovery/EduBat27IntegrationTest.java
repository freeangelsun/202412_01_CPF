package com.cpf.education.batch.jobpack.recovery;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-27 IntegrationTest — Job Pack Checksum·호환성·이전 Version 복구 */
public final class EduBat27IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat27Handler(); }
}
