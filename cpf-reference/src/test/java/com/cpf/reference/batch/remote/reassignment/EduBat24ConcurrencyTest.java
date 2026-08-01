package com.cpf.reference.batch.remote.reassignment;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-24 ConcurrencyTest — Remote Worker 유실·재할당·중복 결과 차단 */
public final class EduBat24ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat24Handler(); }
}
