package cpf.adm.opr.dto;

/**
 * ADM 영업일 캘린더 저장 요청입니다.
 *
 * @param calendarId 캘린더 ID
 * @param businessDate 영업일 기준 일자
 * @param holidayYn 휴일 여부
 * @param businessDayYn 영업일 여부
 * @param description 운영 설명
 * @param requestUser 요청 운영자 ID
 * @param reason 감사 로그에 남길 운영 사유
 */
public record AdmBusinessDayRequest(
        String calendarId,
        String businessDate,
        String holidayYn,
        String businessDayYn,
        String description,
        String requestUser,
        String reason) {
}
