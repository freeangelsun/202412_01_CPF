package cpf.adm.opr.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 자동 스케줄러가 이번 tick에서 실행 대상으로 판단한 배치 스케줄입니다.
 *
 * @param scheduleId 스케줄 ID입니다.
 * @param jobId 배치 Job ID입니다.
 * @param cronExpression cron 표현식입니다.
 * @param timezone 스케줄 기준 시간대입니다.
 * @param calendarId 영업일 캘린더 ID입니다.
 * @param plannedRunAt 예정 실행 일시입니다.
 * @param nextRunAt 다음 예정 실행 일시입니다.
 * @param businessDate 업무 기준일입니다.
 * @param jobParameters 실행 파라미터 JSON 문자열입니다.
 */
public record PfwBatchScheduleCandidate(
        String scheduleId,
        String jobId,
        String cronExpression,
        String timezone,
        String calendarId,
        LocalDateTime plannedRunAt,
        LocalDateTime nextRunAt,
        LocalDate businessDate,
        String jobParameters
) {
}
