package com.cpf.education.operations.runtime.application;

import com.cpf.education.scenarios.online.generator.domain.EduDev01Handler;
import com.cpf.education.scenarios.online.query.scoped.EduDev02Handler;
import com.cpf.education.scenarios.online.command.audit.EduDev03Handler;
import com.cpf.education.scenarios.online.concurrency.optimisticlock.EduDev04Handler;
import com.cpf.education.scenarios.online.idempotency.payment.EduDev05Handler;
import com.cpf.education.scenarios.online.servicecall.topology.EduDev06Handler;
import com.cpf.education.scenarios.online.messaging.outboxinbox.EduDev07Handler;
import com.cpf.education.scenarios.online.file.attachment.EduDev08Handler;
import com.cpf.education.scenarios.online.counterparty.rest.EduDev09Handler;
import com.cpf.education.scenarios.online.counterparty.fixedwidth.EduDev10Handler;
import com.cpf.education.scenarios.online.security.authorization.EduDev11Handler;
import com.cpf.education.scenarios.online.runtime.featuremanagement.EduDev12Handler;
import com.cpf.education.scenarios.online.notification.export.EduDev13Handler;
import com.cpf.education.scenarios.online.database.migration.EduDev14Handler;
import com.cpf.education.scenarios.online.resilience.recovery.EduDev15Handler;
import com.cpf.education.scenarios.online.query.cursor.EduDev16Handler;
import com.cpf.education.scenarios.online.file.bulkimport.EduDev17Handler;
import com.cpf.education.scenarios.online.lifecycle.softdelete.EduDev18Handler;
import com.cpf.education.scenarios.online.masterdata.effectiveperiod.EduDev19Handler;
import com.cpf.education.scenarios.online.workflow.statemachine.EduDev20Handler;
import com.cpf.education.scenarios.online.messaging.transactionaloutbox.EduDev21Handler;
import com.cpf.education.scenarios.online.workflow.saga.EduDev22Handler;
import com.cpf.education.scenarios.online.contract.validation.EduDev23Handler;
import com.cpf.education.scenarios.online.asyncoperation.lifecycle.EduDev24Handler;
import com.cpf.education.scenarios.online.counterparty.webhook.EduDev25Handler;
import com.cpf.education.scenarios.online.file.sftp.EduDev26Handler;
import com.cpf.education.scenarios.online.counterparty.soap.EduDev27Handler;
import com.cpf.education.scenarios.online.file.multipart.EduDev28Handler;
import com.cpf.education.scenarios.online.file.quarantine.EduDev29Handler;
import com.cpf.education.scenarios.online.file.objectstorage.EduDev30Handler;
import com.cpf.education.scenarios.online.notification.multichannel.EduDev31Handler;
import com.cpf.education.scenarios.online.security.cryptography.EduDev32Handler;
import com.cpf.education.scenarios.online.security.session.EduDev33Handler;
import com.cpf.education.scenarios.online.api.quota.EduDev34Handler;
import com.cpf.education.scenarios.online.runtime.featuretoggle.EduDev35Handler;
import com.cpf.education.scenarios.online.cache.consistency.EduDev36Handler;
import com.cpf.education.scenarios.online.concurrency.lease.EduDev37Handler;
import com.cpf.education.scenarios.online.security.multitenancy.EduDev38Handler;
import com.cpf.education.scenarios.online.calendar.businessday.EduDev39Handler;
import com.cpf.education.scenarios.online.money.exchange.EduDev40Handler;
import com.cpf.education.scenarios.online.audit.evidence.EduDev41Handler;
import com.cpf.education.scenarios.online.observability.correlation.EduDev42Handler;
import com.cpf.education.scenarios.online.api.versioning.EduDev43Handler;
import com.cpf.education.scenarios.online.messaging.schema.EduDev44Handler;
import com.cpf.education.scenarios.online.query.searchindex.EduDev45Handler;
import com.cpf.education.operations.platform.install.artifact.EduOps01Handler;
import com.cpf.education.operations.platform.configuration.validation.EduOps02Handler;
import com.cpf.education.operations.platform.security.secretrotation.EduOps03Handler;
import com.cpf.education.operations.platform.database.lifecycle.EduOps04Handler;
import com.cpf.education.operations.platform.messaging.kafka.EduOps05Handler;
import com.cpf.education.operations.platform.lifecycle.startstop.EduOps06Handler;
import com.cpf.education.operations.platform.deployment.rolling.EduOps07Handler;
import com.cpf.education.operations.platform.deployment.bluegreen.EduOps08Handler;
import com.cpf.education.operations.platform.configuration.reconcile.EduOps09Handler;
import com.cpf.education.operations.platform.observability.pipeline.EduOps10Handler;
import com.cpf.education.operations.platform.recovery.backuprestore.EduOps11Handler;
import com.cpf.education.operations.platform.recovery.disaster.EduOps12Handler;
import com.cpf.education.operations.platform.runbook.infrastructure.EduOps13Handler;
import com.cpf.education.operations.platform.security.incident.EduOps14Handler;
import com.cpf.education.operations.platform.upgrade.compatibility.EduOps15Handler;
import org.springframework.stereotype.Component;
import java.util.*;

/** education-core 기능군의 실행 가능한 Education Handler를 등록합니다. */
@Component
public final class CoreEduCapabilityContributor implements EduCapabilityContributor {
    @Override public String featureId() { return "education-core"; }
    @Override public Collection<? extends AbstractEduCapabilityHandler> handlers() { return List.of(new EduDev01Handler(), new EduDev02Handler(), new EduDev03Handler(), new EduDev04Handler(), new EduDev05Handler(), new EduDev06Handler(), new EduDev07Handler(), new EduDev08Handler(), new EduDev09Handler(), new EduDev10Handler(), new EduDev11Handler(), new EduDev12Handler(), new EduDev13Handler(), new EduDev14Handler(), new EduDev15Handler(), new EduDev16Handler(), new EduDev17Handler(), new EduDev18Handler(), new EduDev19Handler(), new EduDev20Handler(), new EduDev21Handler(), new EduDev22Handler(), new EduDev23Handler(), new EduDev24Handler(), new EduDev25Handler(), new EduDev26Handler(), new EduDev27Handler(), new EduDev28Handler(), new EduDev29Handler(), new EduDev30Handler(), new EduDev31Handler(), new EduDev32Handler(), new EduDev33Handler(), new EduDev34Handler(), new EduDev35Handler(), new EduDev36Handler(), new EduDev37Handler(), new EduDev38Handler(), new EduDev39Handler(), new EduDev40Handler(), new EduDev41Handler(), new EduDev42Handler(), new EduDev43Handler(), new EduDev44Handler(), new EduDev45Handler(), new EduOps01Handler(), new EduOps02Handler(), new EduOps03Handler(), new EduOps04Handler(), new EduOps05Handler(), new EduOps06Handler(), new EduOps07Handler(), new EduOps08Handler(), new EduOps09Handler(), new EduOps10Handler(), new EduOps11Handler(), new EduOps12Handler(), new EduOps13Handler(), new EduOps14Handler(), new EduOps15Handler()); }
}
