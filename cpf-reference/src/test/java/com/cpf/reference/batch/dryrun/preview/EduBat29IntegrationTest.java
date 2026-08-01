package com.cpf.reference.batch.dryrun.preview;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-29 IntegrationTest — Dry Run·건수 Preview·표본 확인 */
public final class EduBat29IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat29Handler(); }
}
