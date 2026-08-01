package com.cpf.reference.optional.backoffice.config;
import com.cpf.reference.edu.runtime.application.*;
import com.cpf.reference.optional.backoffice.organization.EduBackoffice01Handler;
import com.cpf.reference.optional.backoffice.authorization.EduBackoffice02Handler;
import com.cpf.reference.optional.backoffice.policysimulation.EduBackoffice03Handler;
import com.cpf.reference.optional.backoffice.approvalflow.EduBackoffice04Handler;
import com.cpf.reference.optional.backoffice.delegation.EduBackoffice05Handler;
import com.cpf.reference.optional.backoffice.evidence.EduBackoffice06Handler;
import com.cpf.reference.optional.backoffice.directory.EduBackoffice07Handler;
import com.cpf.reference.optional.backoffice.reorganization.EduBackoffice08Handler;
import com.cpf.reference.optional.backoffice.lifecycle.EduBackoffice09Handler;
import com.cpf.reference.optional.backoffice.separationofduties.EduBackoffice10Handler;
import com.cpf.reference.optional.backoffice.approvalhistory.EduBackoffice11Handler;
import com.cpf.reference.optional.backoffice.attachment.EduBackoffice12Handler;
import com.cpf.reference.optional.backoffice.privacyexport.EduBackoffice13Handler;
import com.cpf.reference.optional.backoffice.rollback.EduBackoffice14Handler;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
/** Product-independent reference-backoffice EDU contribution owned only by cpf-reference. */
@Component
@ConditionalOnProperty(name="cpf.reference.features.backoffice.enabled",havingValue="true",matchIfMissing=true)
public final class ReferenceBackofficeCapabilityContributor implements EduCapabilityContributor {
    @Override public String featureId() { return "reference-backoffice"; }
    @Override public Collection<? extends AbstractEduCapabilityHandler> handlers() {
        return List.of(new EduBackoffice01Handler(), new EduBackoffice02Handler(), new EduBackoffice03Handler(), new EduBackoffice04Handler(), new EduBackoffice05Handler(), new EduBackoffice06Handler(), new EduBackoffice07Handler(), new EduBackoffice08Handler(), new EduBackoffice09Handler(), new EduBackoffice10Handler(), new EduBackoffice11Handler(), new EduBackoffice12Handler(), new EduBackoffice13Handler(), new EduBackoffice14Handler());
    }
}
