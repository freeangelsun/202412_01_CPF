package com.cpf.reference.platform.messaging.kafka;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-05 UnitTest — Kafka Topic·ACL·Consumer Group Lifecycle */
public final class EduOps05UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps05Handler(); }
}
