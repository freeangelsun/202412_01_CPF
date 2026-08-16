package com.cpf.education.scenarios.online.messaging.transactionaloutbox;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-21 FailureTest — Transactional Outbox 게시 지연·재시작 */
public final class EduDev21FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev21Handler(); }
}
