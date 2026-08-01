package com.cpf.reference.batch.centercut.approval;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-06 UnitTest — 센터컷 Preview·승인·실행 */
public final class EduBat06UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat06Handler(); }
}
