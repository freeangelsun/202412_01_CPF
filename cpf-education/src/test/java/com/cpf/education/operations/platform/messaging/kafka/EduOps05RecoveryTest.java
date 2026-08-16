package com.cpf.education.operations.platform.messaging.kafka;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-05 RecoveryTest — Kafka Topic·ACL·Consumer Group Lifecycle */
public final class EduOps05RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps05Handler(); }
}
