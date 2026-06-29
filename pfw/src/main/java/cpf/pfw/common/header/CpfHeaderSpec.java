package cpf.pfw.common.header;

/**
 * 표준 헤더 하나에 대한 문서/검증/로그/DB 기준입니다.
 */
public record CpfHeaderSpec(
        String name,
        CpfHeaderCategory category,
        String description,
        String producer,
        String validationPoint,
        boolean propagation,
        boolean responseHeader,
        String dbColumn,
        int recommendedDbLength,
        boolean loggable,
        boolean masked,
        String admSection) {
}
