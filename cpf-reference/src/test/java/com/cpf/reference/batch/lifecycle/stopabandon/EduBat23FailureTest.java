package com.cpf.reference.batch.lifecycle.stopabandon;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-23 FailureTest — Stop·Abandon·Restart 의미 분리 */
public final class EduBat23FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat23Handler(); }
}
