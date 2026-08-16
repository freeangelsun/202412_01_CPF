package com.cpf.education.operations.backoffice.approvalflow;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-04 UnitTest — 상신·승인·반려·철회·취소 */
public final class EduBackoffice04UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice04Handler(); }
}
