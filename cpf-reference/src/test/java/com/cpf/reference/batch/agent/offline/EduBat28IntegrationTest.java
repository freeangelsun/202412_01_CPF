package com.cpf.reference.batch.agent.offline;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-28 IntegrationTest — Host Agent Offline·명령 ACK 유실 */
public final class EduBat28IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat28Handler(); }
}
