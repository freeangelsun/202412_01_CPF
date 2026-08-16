package com.cpf.education.batch.support.config;
import com.cpf.education.operations.runtime.application.*;
import com.cpf.education.batch.tasklet.close.EduBat01Handler;
import com.cpf.education.batch.chunk.membergrade.EduBat02Handler;
import com.cpf.education.batch.file.csv.EduBat03Handler;
import com.cpf.education.batch.partition.range.EduBat04Handler;
import com.cpf.education.batch.remote.worker.EduBat05Handler;
import com.cpf.education.batch.centercut.approval.EduBat06Handler;
import com.cpf.education.batch.scheduler.businessday.EduBat07Handler;
import com.cpf.education.batch.jobpack.version.EduBat08Handler;
import com.cpf.education.batch.recovery.restart.EduBat09Handler;
// 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
import com.cpf.education.batch.reconcile.requestloss.EduBat10Handler;
import com.cpf.education.batch.flow.conditional.EduBat11Handler;
import com.cpf.education.batch.faulttolerance.retryskip.EduBat12Handler;
import com.cpf.education.batch.checkpoint.writercommit.EduBat13Handler;
import com.cpf.education.batch.instance.parameter.EduBat14Handler;
import com.cpf.education.batch.backfill.latearrival.EduBat15Handler;
import com.cpf.education.batch.incremental.watermark.EduBat16Handler;
import com.cpf.education.batch.file.secureoutput.EduBat17Handler;
import com.cpf.education.batch.file.validation.EduBat18Handler;
import com.cpf.education.batch.file.faninout.EduBat19Handler;
import com.cpf.education.batch.scheduler.misfire.EduBat20Handler;
import com.cpf.education.batch.concurrency.execution.EduBat21Handler;
import com.cpf.education.batch.calendar.businessday.EduBat22Handler;
import com.cpf.education.batch.lifecycle.stopabandon.EduBat23Handler;
import com.cpf.education.batch.remote.reassignment.EduBat24Handler;
import com.cpf.education.batch.partition.rebalance.EduBat25Handler;
// 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
import com.cpf.education.batch.centercut.reconcile.EduBat26Handler;
import com.cpf.education.batch.jobpack.recovery.EduBat27Handler;
import com.cpf.education.batch.agent.offline.EduBat28Handler;
import com.cpf.education.batch.dryrun.preview.EduBat29Handler;
import com.cpf.education.batch.performance.backpressure.EduBat30Handler;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
/** Product-independent education-batch EDU contribution owned only by cpf-education. */
@Component
@ConditionalOnProperty(name="cpf.education.features.batch.enabled",havingValue="true",matchIfMissing=true)
/** EducationBatchCapabilityContributor 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class EducationBatchCapabilityContributor implements EduCapabilityContributor {
    @Override public String featureId() { return "education-batch"; }
    @Override public Collection<? extends AbstractEduCapabilityHandler> handlers() {
        return List.of(new EduBat01Handler(), new EduBat02Handler(), new EduBat03Handler(), new EduBat04Handler(), new EduBat05Handler(), new EduBat06Handler(), new EduBat07Handler(), new EduBat08Handler(), new EduBat09Handler(), new EduBat10Handler(), new EduBat11Handler(), new EduBat12Handler(), new EduBat13Handler(), new EduBat14Handler(), new EduBat15Handler(), new EduBat16Handler(), new EduBat17Handler(), new EduBat18Handler(), new EduBat19Handler(), new EduBat20Handler(), new EduBat21Handler(), new EduBat22Handler(), new EduBat23Handler(), new EduBat24Handler(), new EduBat25Handler(), new EduBat26Handler(), new EduBat27Handler(), new EduBat28Handler(), new EduBat29Handler(), new EduBat30Handler());
    }
}
