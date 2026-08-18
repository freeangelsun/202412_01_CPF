package com.cpf.education.batch.scheduler;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import com.cpf.common.calendar.api.CpfCalendarService;
import java.time.LocalDate;
import java.util.Map;

/** 배치-06 Scheduler·영업일: 영업일, misfire, 예정/실제 실행시간을 Batch Runtime Context로 처리합니다. */
@CpfBatchJob(value = "EDU_BUSINESS_DATE_JOB")
public class BusinessDateSchedulerJob {
    private final CpfCalendarService calendar;

    public BusinessDateSchedulerJob(CpfCalendarService calendar) {
        this.calendar = calendar;
    }

    @CpfBatchStep(value = "business-day", order = 1)
    /** run 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public BatchStepResult run(BatchStepCommand command) {
        LocalDate date = LocalDate.parse(String.valueOf(
                command.jobParameters().getOrDefault("businessDate", LocalDate.now().toString())));
        boolean misfire = Boolean.parseBoolean(String.valueOf(command.jobParameters().getOrDefault("misfire", false)));
        String scheduledAt = String.valueOf(command.jobParameters().getOrDefault("scheduledAt", ""));
        String actualAt = String.valueOf(command.jobParameters().getOrDefault("actualAt", ""));

        if (misfire) {
            return new BatchStepResult(
                    BatchStepResult.Status.STOPPED,
                    "MISFIRE_REVIEW",
                    "Misfire 정책에 따라 자동 중복 실행하지 않고 Runtime 정책으로 재계획합니다.",
                    0, 0, 0,
                    Map.of("businessDate", date.toString(), "scheduledAt", scheduledAt, "actualAt", actualAt));
        }
        if (!calendar.isBusinessDay("KR", date)) {
            return new BatchStepResult(
                    BatchStepResult.Status.STOPPED,
                    "NON_BUSINESS_DAY",
                    "영업일이 아닙니다.",
                    0, 0, 0,
                    Map.of("businessDate", date.toString()));
        }
        return BatchStepResult.completed(
                "scheduled business-day job",
                0, 0,
                Map.of("businessDate", date.toString(), "scheduledAt", scheduledAt, "actualAt", actualAt));
    }
}
