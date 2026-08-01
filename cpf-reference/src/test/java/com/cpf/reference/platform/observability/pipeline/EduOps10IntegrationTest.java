package com.cpf.reference.platform.observability.pipeline;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-10 IntegrationTest — Log·Metric·Trace 수집 장애·보존·용량 */
public final class EduOps10IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps10Handler(); }
}
