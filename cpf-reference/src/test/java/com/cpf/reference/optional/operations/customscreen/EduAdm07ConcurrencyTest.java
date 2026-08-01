package com.cpf.reference.optional.operations.customscreen;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-ADM-07 ConcurrencyTest — 고객 전용 화면 추가의 마지막 선택 */
public final class EduAdm07ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm07Handler(); }
}
