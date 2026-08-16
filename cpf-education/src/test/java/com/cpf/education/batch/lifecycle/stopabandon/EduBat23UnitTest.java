package com.cpf.education.batch.lifecycle.stopabandon;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-23 UnitTest — Stop·Abandon·Restart 의미 분리 */
public final class EduBat23UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat23Handler(); }
}
