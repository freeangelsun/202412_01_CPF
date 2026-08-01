package com.cpf.reference.optional.backoffice.reorganization;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-08 FailureTest — 조직 개편·기준일·과거 이력 유지 */
public final class EduBackoffice08FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice08Handler(); }
}
