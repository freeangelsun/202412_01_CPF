package com.cpf.reference.batch.dryrun.preview;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-29 ConcurrencyTest — Dry Run·건수 Preview·표본 확인 */
public final class EduBat29ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat29Handler(); }
}
