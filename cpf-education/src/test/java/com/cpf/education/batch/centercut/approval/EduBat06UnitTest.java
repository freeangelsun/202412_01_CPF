package com.cpf.education.batch.centercut.approval;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-06 UnitTest — 센터컷 Preview·승인·실행 */
public final class EduBat06UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat06Handler(); }
}
