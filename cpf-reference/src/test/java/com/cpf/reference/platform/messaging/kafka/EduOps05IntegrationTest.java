package com.cpf.reference.platform.messaging.kafka;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-05 IntegrationTest — Kafka Topic·ACL·Consumer Group Lifecycle */
public final class EduOps05IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps05Handler(); }
}
