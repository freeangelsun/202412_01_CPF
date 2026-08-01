package com.cpf.reference.online.api.quota;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-34 ConcurrencyTest — API 사용량 제한·고객별 Quota·초과 처리 */
public final class EduDev34ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev34Handler(); }
}
