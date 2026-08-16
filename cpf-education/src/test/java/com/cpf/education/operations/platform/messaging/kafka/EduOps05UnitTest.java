package com.cpf.education.operations.platform.messaging.kafka;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-05 UnitTest — Kafka Topic·ACL·Consumer Group Lifecycle */
public final class EduOps05UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps05Handler(); }
}
