package com.cpf.reference.optional.backoffice.organization;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-01 FailureTest — 조직·직원·발령·기준일 */
public final class EduBackoffice01FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice01Handler(); }
}
