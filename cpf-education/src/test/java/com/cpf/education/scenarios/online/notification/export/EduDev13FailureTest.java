package com.cpf.education.scenarios.online.notification.export;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-13 FailureTest — 알림·비동기 내보내기·다운로드 감사 */
public final class EduDev13FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev13Handler(); }
}
