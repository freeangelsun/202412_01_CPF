package com.cpf.reference.optional.operations.search;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-ADM-08 UnitTest — 권한·데이터 범위·Masking·사유 입력 연동 */
public final class EduAdm08UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm08Handler(); }
}
