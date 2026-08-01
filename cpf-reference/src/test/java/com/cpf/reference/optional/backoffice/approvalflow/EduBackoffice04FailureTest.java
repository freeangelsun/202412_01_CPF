package com.cpf.reference.optional.backoffice.approvalflow;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-04 FailureTest — 상신·승인·반려·철회·취소 */
public final class EduBackoffice04FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice04Handler(); }
}
