package com.cpf.education.batch.agent.offline;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-28 ConcurrencyTest — Host Agent Offline·명령 ACK 유실 */
public final class EduBat28ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat28Handler(); }
}
