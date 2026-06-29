package cpf.pfw.common.header;

/**
 * CPF 표준 헤더의 운영 분류입니다.
 *
 * <p>분류는 문서, 검증, 로그 저장, ADM 화면 표시 기준을 맞추기 위한 값입니다.</p>
 */
public enum CpfHeaderCategory {
    REQUIRED,
    RECOMMENDED,
    OPTIONAL,
    INTERNAL_ONLY,
    FORBIDDEN_TO_LOG_RAW
}
