package com.cpf.reference.platform.observability.pipeline;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-10 UnitTest — Log·Metric·Trace 수집 장애·보존·용량 */
public final class EduOps10UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps10Handler(); }
}
