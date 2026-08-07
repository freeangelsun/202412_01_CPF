package com.cpf.reference.optional.operations.config;

import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.application.EduCapabilityContributor;
import com.cpf.reference.optional.operations.approval.EduAdm04Handler;
import com.cpf.reference.optional.operations.command.EduAdm03Handler;
import com.cpf.reference.optional.operations.customscreen.EduAdm07Handler;
import com.cpf.reference.optional.operations.query.EduAdm02Handler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Adopter-facing ADM extension samples only.
 *
 * <p>R6J architecture decision: Product ADM capabilities must execute in cpf-admin, not in
 * cpf-reference. Generic patterns that duplicate an existing DEV/OPS EDU are represented in
 * the manual catalog as MERGED_REFERENCE redirects and are not independently executable.
 * This contributor therefore exposes only the four retained public extension examples.
 */
@Component
@ConditionalOnProperty(name="cpf.reference.features.operations.enabled", havingValue="true", matchIfMissing=true)
public final class ReferenceOperationsCapabilityContributor implements EduCapabilityContributor {
    @Override public String featureId() { return "reference-operations"; }

    @Override
    public Collection<? extends AbstractEduCapabilityHandler> handlers() {
        return List.of(
                new EduAdm02Handler(),
                new EduAdm03Handler(),
                new EduAdm04Handler(),
                new EduAdm07Handler());
    }
}
