package com.cpf.education.operations.platform.observability.pipeline;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-10 ConcurrencyTest — Log·Metric·Trace 수집 장애·보존·용량 */
public final class EduOps10ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps10Handler(); }
}
