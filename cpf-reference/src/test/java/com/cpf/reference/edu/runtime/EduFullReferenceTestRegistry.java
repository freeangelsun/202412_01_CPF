package com.cpf.reference.edu.runtime;

import com.cpf.reference.batch.config.ReferenceBatchCapabilityContributor;
import com.cpf.reference.edu.runtime.application.CoreEduCapabilityContributor;
import com.cpf.reference.edu.runtime.application.EduCapabilityRegistry;
import com.cpf.reference.optional.backoffice.config.ReferenceBackofficeCapabilityContributor;
import com.cpf.reference.optional.gateway.config.ReferenceGatewayCapabilityContributor;
import com.cpf.reference.optional.operations.config.ReferenceOperationsCapabilityContributor;

import java.util.List;

/** Typed contributor registry for the full 135-capability Reference test variant. */
final class EduFullReferenceTestRegistry {
    private EduFullReferenceTestRegistry() {}

    static EduCapabilityRegistry create() {
        return new EduCapabilityRegistry(List.of(
                new CoreEduCapabilityContributor(),
                new ReferenceBatchCapabilityContributor(),
                new ReferenceBackofficeCapabilityContributor(),
                new ReferenceOperationsCapabilityContributor(),
                new ReferenceGatewayCapabilityContributor()));
    }
}
