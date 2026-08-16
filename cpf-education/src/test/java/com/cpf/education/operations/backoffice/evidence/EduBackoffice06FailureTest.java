package com.cpf.education.operations.backoffice.evidence;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-06 FailureTest — 첨부·알림·감사·다운로드 */
public final class EduBackoffice06FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice06Handler(); }
}
