package cpf.cmn.message.fixedlength;

import java.util.List;

/**
 * 반복부를 표현하기 위한 skeleton입니다.
 *
 * @param name 반복부 이름
 * @param countFieldName 반복 건수를 담는 필드명
 * @param maxCount 허용 최대 반복 건수
 * @param fields 반복 항목 내부 필드
 */
public record FixedLengthGroupSpec(
        String name,
        String countFieldName,
        int maxCount,
        List<FixedLengthFieldSpec> fields) {

    public FixedLengthGroupSpec {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("반복부 이름은 필수입니다.");
        }
        if (maxCount < 1) {
            throw new IllegalArgumentException("반복부 최대 건수는 1 이상이어야 합니다. name=" + name);
        }
        fields = fields == null ? List.of() : List.copyOf(fields);
    }
}
