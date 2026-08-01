package com.cpf.reference.online.notification.export;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-13 UnitTest — 알림·비동기 내보내기·다운로드 감사 */
public final class EduDev13UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev13Handler(); }
}
