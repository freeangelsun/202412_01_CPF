package com.cpf.reference.batch.dryrun.preview;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-29 UnitTest — Dry Run·건수 Preview·표본 확인 */
public final class EduBat29UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat29Handler(); }
}
