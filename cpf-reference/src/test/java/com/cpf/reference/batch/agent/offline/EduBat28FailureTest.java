package com.cpf.reference.batch.agent.offline;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-28 FailureTest — Host Agent Offline·명령 ACK 유실 */
public final class EduBat28FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat28Handler(); }
}
