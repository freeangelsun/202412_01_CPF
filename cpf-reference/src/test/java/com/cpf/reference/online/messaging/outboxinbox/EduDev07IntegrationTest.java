package com.cpf.reference.online.messaging.outboxinbox;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-07 IntegrationTest — Kafka Outbox·Inbox·중복 소비·재처리 */
public final class EduDev07IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev07Handler(); }
}
