package com.cpf.reference.platform.messaging.kafka;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-05 RecoveryTest — Kafka Topic·ACL·Consumer Group Lifecycle */
public final class EduOps05RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps05Handler(); }
}
