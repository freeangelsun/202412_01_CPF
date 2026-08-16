package com.cpf.education.batch.lifecycle.stopabandon;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-23 ConcurrencyTest — Stop·Abandon·Restart 의미 분리 */
public final class EduBat23ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat23Handler(); }
}
