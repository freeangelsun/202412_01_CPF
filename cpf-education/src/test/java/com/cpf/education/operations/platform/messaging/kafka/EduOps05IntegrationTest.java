package com.cpf.education.operations.platform.messaging.kafka;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-05 IntegrationTest — Kafka Topic·ACL·Consumer Group Lifecycle */
public final class EduOps05IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps05Handler(); }
}
