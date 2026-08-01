package com.cpf.reference.batch.concurrency.execution;
import com.cpf.reference.batch.runtime.EduBatchScenarioWorker;
import com.cpf.reference.edu.runtime.model.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.*;
/** EDU-BAT-21 실제 Spring Batch Job 진입점. 고객 Job은 cpf-batch Runtime에 적치하지 않습니다. */
@Configuration(proxyBeanMethods=false)
@ConditionalOnProperty(name="cpf.reference.features.batch.enabled",havingValue="true",matchIfMissing=true)
public class EduBat21JobConfiguration {
 @Bean("eduBat21Job") Job eduBat21Job(JobRepository repository, Step eduBat21Step) {
  return new JobBuilder("EDU-BAT-21.job", repository).start(eduBat21Step).build();
 }
 @Bean("eduBat21Step") Step eduBat21Step(JobRepository repository, PlatformTransactionManager transactionManager, EduBatchScenarioWorker worker) {
  return new StepBuilder("EDU-BAT-21.step", repository).tasklet((contribution, chunkContext) -> {
   var p=chunkContext.getStepContext().getJobParameters(); Map<String,Object> payload=new LinkedHashMap<>();
   for(String field:List.of("jobKey", "businessDate", "scopeKey")) { Object value=p.get("edu."+field); if(value!=null)payload.put(field,value); }
   String businessKey=String.valueOf(p.getOrDefault("cpf.businessKey", "EDU-BAT-21-"+UUID.randomUUID()));
   String reason=String.valueOf(p.getOrDefault("edu.reason", "scheduled batch execution"));
   long fencingToken=longValue(p.get("cpf.fencingToken"));
   String idempotencyKey=String.valueOf(p.getOrDefault("cpf.idempotencyKey", businessKey));
   int written=worker.execute("EDU-BAT-21",businessKey,String.valueOf(p.getOrDefault("cpf.dataScope","BATCH")),idempotencyKey,fencingToken,payload);
   contribution.incrementWriteCount(Math.max(1,written));
   return org.springframework.batch.repeat.RepeatStatus.FINISHED;
  }, transactionManager).build();
 }
 private static long longValue(Object value) { return value==null?0L:Long.parseLong(String.valueOf(value)); }
}
