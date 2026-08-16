package com.cpf.education.operations.runtime;
import com.cpf.education.batch.support.config.EducationBatchCapabilityContributor;
import com.cpf.education.operations.runtime.application.CoreEduCapabilityContributor;
import com.cpf.education.operations.runtime.application.EduCapabilityRegistry;
import com.cpf.education.operations.runtime.application.EducationBackofficeCapabilityContributor;
import com.cpf.education.operations.runtime.application.EducationGatewayCapabilityContributor;
import com.cpf.education.operations.runtime.application.EducationOperationsCapabilityContributor;

import java.util.List;

/** Typed contributor registry for the full executable Education test variant (122 executable capabilities / 135 manual topics). */
final class EduFullEducationTestRegistry {
    private EduFullEducationTestRegistry() {}

    static EduCapabilityRegistry create() {
        return new EduCapabilityRegistry(List.of(
                new CoreEduCapabilityContributor(),
                new EducationBatchCapabilityContributor(),
                new EducationBackofficeCapabilityContributor(),
                new EducationOperationsCapabilityContributor(),
                new EducationGatewayCapabilityContributor()));
    }
}
