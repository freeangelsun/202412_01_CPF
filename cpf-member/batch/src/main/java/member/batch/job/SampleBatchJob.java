package member.batch.job;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;

/** Generator가 생성하는 최소 Batch Golden Path. 실제 업무 Job은 이 구조를 확장합니다. */
@CpfBatchJob(value="MBR_SAMPLE_BATCH", restartable=true)
public class SampleBatchJob { @CpfBatchStep(value="sampleStep",order=1,idempotent=true) public void execute() { } }
