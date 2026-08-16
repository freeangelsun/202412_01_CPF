package com.cpf.education.operations.runtime.application;

import com.cpf.education.operations.backoffice.organization.EduBackoffice01Handler;
import com.cpf.education.operations.backoffice.authorization.EduBackoffice02Handler;
import com.cpf.education.operations.backoffice.policysimulation.EduBackoffice03Handler;
import com.cpf.education.operations.backoffice.approvalflow.EduBackoffice04Handler;
import com.cpf.education.operations.backoffice.delegation.EduBackoffice05Handler;
import com.cpf.education.operations.backoffice.evidence.EduBackoffice06Handler;
import com.cpf.education.operations.backoffice.directory.EduBackoffice07Handler;
import com.cpf.education.operations.backoffice.reorganization.EduBackoffice08Handler;
import com.cpf.education.operations.backoffice.lifecycle.EduBackoffice09Handler;
import com.cpf.education.operations.backoffice.separationofduties.EduBackoffice10Handler;
import com.cpf.education.operations.backoffice.approvalhistory.EduBackoffice11Handler;
import com.cpf.education.operations.backoffice.attachment.EduBackoffice12Handler;
import com.cpf.education.operations.backoffice.privacyexport.EduBackoffice13Handler;
import com.cpf.education.operations.backoffice.rollback.EduBackoffice14Handler;
import org.springframework.stereotype.Component;
import java.util.*;

/** education-backoffice 기능군의 실행 가능한 Education Handler를 등록합니다. */
@Component
public final class EducationBackofficeCapabilityContributor implements EduCapabilityContributor {
    @Override public String featureId() { return "education-backoffice"; }
    @Override public Collection<? extends AbstractEduCapabilityHandler> handlers() { return List.of(new EduBackoffice01Handler(), new EduBackoffice02Handler(), new EduBackoffice03Handler(), new EduBackoffice04Handler(), new EduBackoffice05Handler(), new EduBackoffice06Handler(), new EduBackoffice07Handler(), new EduBackoffice08Handler(), new EduBackoffice09Handler(), new EduBackoffice10Handler(), new EduBackoffice11Handler(), new EduBackoffice12Handler(), new EduBackoffice13Handler(), new EduBackoffice14Handler()); }
}
