package com.cpf.education.batch.agent.offline;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-28 UnitTest — Host Agent Offline·명령 ACK 유실 */
public final class EduBat28UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat28Handler(); }
}
