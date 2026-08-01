package com.cpf.reference.batch.operation;
import com.cpf.reference.edu.runtime.application.*;
import com.cpf.reference.edu.runtime.consumer.*;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import java.util.*;
/** Executes the real Spring Batch Job bean named by each BAT requirement binding. */
public final class SpringBatchEduBusinessConsumer implements EduBusinessConsumer {
    private final ApplicationContext context; private final JobLauncher launcher;
    public SpringBatchEduBusinessConsumer(ApplicationContext context,JobLauncher launcher){this.context=context;this.launcher=launcher;}
    @Override public EduConsumerType type(){return EduConsumerType.SPRING_BATCH;}
    @Override public EduBusinessConsumerResult invoke(EduConsumerBinding b,EduExecutionCommand c,long fence){
        try{
            Job job=context.getBean(b.entryPoint(),Job.class);JobParametersBuilder p=new JobParametersBuilder().addString("cpf.requirementId",b.requirementId()).addString("cpf.businessKey",c.businessKey()).addString("cpf.idempotencyKey",c.idempotencyKey()).addString("cpf.dataScope",c.dataScope()).addString("cpf.requestId",c.requestId()).addString("cpf.traceId",c.traceId()).addLong("cpf.fencingToken",fence);
            for(var e:c.payload().entrySet())p.addString("edu."+e.getKey(),String.valueOf(e.getValue()));JobExecution x=launcher.run(job,p.toJobParameters());BatchStatus status=x.getStatus();if(status==BatchStatus.FAILED||status==BatchStatus.ABANDONED)throw new IllegalStateException("Spring Batch execution failed status="+status+" id="+x.getId());
            return EduBusinessConsumerResult.completed("SPRING_BATCH_"+status,Map.of("jobName",job.getName(),"jobExecutionId",x.getId(),"status",status.name()));
        }catch(EduConflictException e){throw e;}catch(Exception e){throw new IllegalStateException("Spring Batch consumer failed: "+e.getMessage(),e);}
    }
}
