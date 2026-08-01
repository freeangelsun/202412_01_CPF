package com.cpf.reference.batch.lifecycle.stopabandon;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-23 ConcurrencyTest — Stop·Abandon·Restart 의미 분리 */
public final class EduBat23ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat23Handler(); }
}
