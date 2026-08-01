package com.cpf.reference.platform.messaging.kafka;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-05 FailureTest — Kafka Topic·ACL·Consumer Group Lifecycle */
public final class EduOps05FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps05Handler(); }
}
