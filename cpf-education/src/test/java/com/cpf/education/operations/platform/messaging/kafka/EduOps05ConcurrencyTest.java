package com.cpf.education.operations.platform.messaging.kafka;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-05 ConcurrencyTest — Kafka Topic·ACL·Consumer Group Lifecycle */
public final class EduOps05ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps05Handler(); }
}
