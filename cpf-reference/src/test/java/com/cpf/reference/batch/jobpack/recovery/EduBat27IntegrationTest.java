package com.cpf.reference.batch.jobpack.recovery;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-27 IntegrationTest — Job Pack Checksum·호환성·이전 Version 복구 */
public final class EduBat27IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat27Handler(); }
}
