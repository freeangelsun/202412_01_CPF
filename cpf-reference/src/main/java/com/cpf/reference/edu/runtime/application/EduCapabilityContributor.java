package com.cpf.reference.edu.runtime.application;
import java.util.Collection;
/** Contributes one independently removable CPF Reference EDU feature family. */
public interface EduCapabilityContributor {
    String featureId();
    Collection<? extends AbstractEduCapabilityHandler> handlers();
}
