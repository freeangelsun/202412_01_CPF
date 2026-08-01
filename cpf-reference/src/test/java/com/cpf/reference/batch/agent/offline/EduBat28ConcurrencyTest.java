package com.cpf.reference.batch.agent.offline;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-28 ConcurrencyTest — Host Agent Offline·명령 ACK 유실 */
public final class EduBat28ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat28Handler(); }
}
