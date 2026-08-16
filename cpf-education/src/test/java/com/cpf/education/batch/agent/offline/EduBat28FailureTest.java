package com.cpf.education.batch.agent.offline;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-28 FailureTest — Host Agent Offline·명령 ACK 유실 */
public final class EduBat28FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat28Handler(); }
}
