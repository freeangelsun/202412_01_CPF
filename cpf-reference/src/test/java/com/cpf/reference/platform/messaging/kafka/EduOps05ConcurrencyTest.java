package com.cpf.reference.platform.messaging.kafka;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-05 ConcurrencyTest — Kafka Topic·ACL·Consumer Group Lifecycle */
public final class EduOps05ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps05Handler(); }
}
