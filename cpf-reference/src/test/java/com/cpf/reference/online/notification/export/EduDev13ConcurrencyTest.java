package com.cpf.reference.online.notification.export;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-13 ConcurrencyTest — 알림·비동기 내보내기·다운로드 감사 */
public final class EduDev13ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev13Handler(); }
}
