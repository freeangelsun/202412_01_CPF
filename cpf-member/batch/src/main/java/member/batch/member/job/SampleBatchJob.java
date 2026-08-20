package member.batch.member.job;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;

/** Generator가 생성하는 Feature-First Batch Golden Path. 실제 업무 Job은 같은 Feature Owner 아래 역할별로 확장합니다. */
@CpfBatchJob(value="MBR_SAMPLE_BATCH", restartable=true)
public class SampleBatchJob { @CpfBatchStep(value="sampleStep",order=1,idempotent=true) public void execute() { } }
