package cpf.pfw.common.header;

/**
 * CPF 표준 헤더를 운영 기준에 맞게 분류합니다.
 *
 * <p>분류 값은 문서, 검증, 로그 저장, ADM 화면 표시, 하위 호출 전파 정책이 같은 기준을 보도록
 * 유지해야 합니다.</p>
 */
public enum CpfHeaderCategory {
    REQUIRED,
    RECOMMENDED,
    OPTIONAL,
    INTERNAL_ONLY,
    FORBIDDEN_TO_LOG_RAW
}
