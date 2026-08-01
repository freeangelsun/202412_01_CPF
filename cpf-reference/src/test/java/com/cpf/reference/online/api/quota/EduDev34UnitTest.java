package com.cpf.reference.online.api.quota;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-34 UnitTest — API 사용량 제한·고객별 Quota·초과 처리 */
public final class EduDev34UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev34Handler(); }
}
