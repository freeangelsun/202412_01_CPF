package com.cpf.education.scenarios.online.messaging.outboxinbox;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-07 FailureTest — Kafka Outbox·Inbox·중복 소비·재처리 */
public final class EduDev07FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev07Handler(); }
}
