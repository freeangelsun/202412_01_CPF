package com.cpf.reference.online.query.scoped;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-02 ConcurrencyTest — 권한·범위가 적용된 목록·상세 조회 */
public final class EduDev02ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev02Handler(); }
}
