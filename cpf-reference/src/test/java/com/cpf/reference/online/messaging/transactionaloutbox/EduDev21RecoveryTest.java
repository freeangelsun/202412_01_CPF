package com.cpf.reference.online.messaging.transactionaloutbox;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-21 RecoveryTest — Transactional Outbox 게시 지연·재시작 */
public final class EduDev21RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev21Handler(); }
}
