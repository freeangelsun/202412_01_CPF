package com.cpf.reference.online.lifecycle.softdelete;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-18 UnitTest — 논리 삭제·복원·보존기간 만료 */
public final class EduDev18UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev18Handler(); }
}
