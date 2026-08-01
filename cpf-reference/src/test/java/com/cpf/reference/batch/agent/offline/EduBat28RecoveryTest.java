package com.cpf.reference.batch.agent.offline;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-28 RecoveryTest — Host Agent Offline·명령 ACK 유실 */
public final class EduBat28RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat28Handler(); }
}
