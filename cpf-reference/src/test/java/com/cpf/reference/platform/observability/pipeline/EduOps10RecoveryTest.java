package com.cpf.reference.platform.observability.pipeline;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-10 RecoveryTest — Log·Metric·Trace 수집 장애·보존·용량 */
public final class EduOps10RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps10Handler(); }
}
