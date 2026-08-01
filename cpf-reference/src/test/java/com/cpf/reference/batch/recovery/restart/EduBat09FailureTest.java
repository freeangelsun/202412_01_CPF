package com.cpf.reference.batch.recovery.restart;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-09 FailureTest — 중지·재시작·실패건 재처리 */
public final class EduBat09FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat09Handler(); }
}
