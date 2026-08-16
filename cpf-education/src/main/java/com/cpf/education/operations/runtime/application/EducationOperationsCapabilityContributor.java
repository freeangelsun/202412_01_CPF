package com.cpf.education.operations.runtime.application;

import com.cpf.education.operations.admin.reuse.EduAdm01Handler;
import com.cpf.education.operations.admin.query.EduAdm02Handler;
import com.cpf.education.operations.admin.command.EduAdm03Handler;
import com.cpf.education.operations.admin.approval.EduAdm04Handler;
import com.cpf.education.operations.admin.asyncoperation.EduAdm05Handler;
import com.cpf.education.operations.admin.partialrecovery.EduAdm06Handler;
import com.cpf.education.operations.admin.customscreen.EduAdm07Handler;
import com.cpf.education.operations.admin.search.EduAdm08Handler;
import com.cpf.education.operations.admin.detail.EduAdm09Handler;
import com.cpf.education.operations.admin.bulk.EduAdm10Handler;
import com.cpf.education.operations.admin.configuration.EduAdm11Handler;
import com.cpf.education.operations.admin.incident.EduAdm12Handler;
import com.cpf.education.operations.admin.evidence.EduAdm13Handler;
import com.cpf.education.operations.admin.topology.EduAdm14Handler;
import com.cpf.education.operations.admin.correlation.EduAdm15Handler;
import com.cpf.education.operations.admin.notification.EduAdm16Handler;
import com.cpf.education.operations.admin.session.EduAdm17Handler;
import org.springframework.stereotype.Component;
import java.util.*;

/** education-operations 기능군의 실행 가능한 Education Handler를 등록합니다. */
@Component
public final class EducationOperationsCapabilityContributor implements EduCapabilityContributor {
    @Override public String featureId() { return "education-operations"; }
    @Override public Collection<? extends AbstractEduCapabilityHandler> handlers() { return List.of(new EduAdm01Handler(), new EduAdm02Handler(), new EduAdm03Handler(), new EduAdm04Handler(), new EduAdm05Handler(), new EduAdm06Handler(), new EduAdm07Handler(), new EduAdm08Handler(), new EduAdm09Handler(), new EduAdm10Handler(), new EduAdm11Handler(), new EduAdm12Handler(), new EduAdm13Handler(), new EduAdm14Handler(), new EduAdm15Handler(), new EduAdm16Handler(), new EduAdm17Handler()); }
}
