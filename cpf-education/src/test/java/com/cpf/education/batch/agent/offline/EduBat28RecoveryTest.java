package com.cpf.education.batch.agent.offline;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-28 RecoveryTest — Host Agent Offline·명령 ACK 유실 */
public final class EduBat28RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat28Handler(); }
}
