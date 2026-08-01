package com.cpf.reference.online.messaging.outboxinbox;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-07 RecoveryTest — Kafka Outbox·Inbox·중복 소비·재처리 */
public final class EduDev07RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev07Handler(); }
}
