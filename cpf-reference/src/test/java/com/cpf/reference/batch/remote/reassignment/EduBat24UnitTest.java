package com.cpf.reference.batch.remote.reassignment;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-24 UnitTest — Remote Worker 유실·재할당·중복 결과 차단 */
public final class EduBat24UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat24Handler(); }
}
