package com.cpf.reference.online.messaging.transactionaloutbox;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-21 IntegrationTest — Transactional Outbox 게시 지연·재시작 */
public final class EduDev21IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev21Handler(); }
}
