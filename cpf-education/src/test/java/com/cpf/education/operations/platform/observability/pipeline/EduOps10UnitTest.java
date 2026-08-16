package com.cpf.education.operations.platform.observability.pipeline;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-10 UnitTest — Log·Metric·Trace 수집 장애·보존·용량 */
public final class EduOps10UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps10Handler(); }
}
