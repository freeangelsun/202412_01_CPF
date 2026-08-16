package com.cpf.education.operations.runtime.application;
import java.util.Collection;
/** Contributes one independently removable CPF Education feature family. */
/** EduCapabilityContributor 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface EduCapabilityContributor {
    String featureId();
    Collection<? extends AbstractEduCapabilityHandler> handlers();
}
