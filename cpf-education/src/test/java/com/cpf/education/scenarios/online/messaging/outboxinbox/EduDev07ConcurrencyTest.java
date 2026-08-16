package com.cpf.education.scenarios.online.messaging.outboxinbox;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-07 ConcurrencyTest — Kafka Outbox·Inbox·중복 소비·재처리 */
public final class EduDev07ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev07Handler(); }
}
