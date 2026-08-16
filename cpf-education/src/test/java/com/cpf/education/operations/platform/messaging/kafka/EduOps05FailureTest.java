package com.cpf.education.operations.platform.messaging.kafka;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-05 FailureTest — Kafka Topic·ACL·Consumer Group Lifecycle */
public final class EduOps05FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps05Handler(); }
}
