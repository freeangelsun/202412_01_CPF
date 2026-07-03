package cpf.cmn.message.fixedlength;

import java.util.List;

/**
 * 반복부를 표현하기 위한 skeleton입니다.
 *
 * @param name 반복부 이름
 * @param countFieldName 반복 건수를 담는 필드명
 * @param start 1부터 시작하는 반복부 byte 시작 위치
 * @param maxCount 허용 최대 반복 건수
 * @param fields 반복 항목 내부 필드
 */
public record FixedLengthGroupSpec(
        String name,
        String countFieldName,
        int start,
        int maxCount,
        List<FixedLengthFieldSpec> fields) {

    public FixedLengthGroupSpec(String name, String countFieldName, int maxCount, List<FixedLengthFieldSpec> fields) {
        this(name, countFieldName, 0, maxCount, fields);
    }

    public FixedLengthGroupSpec {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("반복부 이름은 필수입니다.");
        }
        if (start < 0) {
            throw new IllegalArgumentException("반복부 시작 위치는 0 이상이어야 합니다. name=" + name);
        }
        if (maxCount < 1) {
            throw new IllegalArgumentException("반복부 최대 건수는 1 이상이어야 합니다. name=" + name);
        }
        fields = fields == null ? List.of() : List.copyOf(fields);
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("반복부 필드 정의가 필요합니다. name=" + name);
        }
    }

    public int zeroBasedStart(int fallbackStart) {
        return start > 0 ? start - 1 : fallbackStart;
    }

    public int itemLength() {
        return fields.stream()
                .mapToInt(FixedLengthFieldSpec::zeroBasedEndExclusive)
                .max()
                .orElse(0);
    }
}
