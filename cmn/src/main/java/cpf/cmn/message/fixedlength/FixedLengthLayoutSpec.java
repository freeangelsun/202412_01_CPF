package cpf.cmn.message.fixedlength;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

/**
 * 고정길이 전문 전체 레이아웃 정의입니다.
 *
 * @param charset 전문 인코딩
 * @param totalLength 전체 byte 길이
 * @param fields 필드 목록
 * @param groups 반복부 목록
 */
public record FixedLengthLayoutSpec(
        Charset charset,
        int totalLength,
        List<FixedLengthFieldSpec> fields,
        List<FixedLengthGroupSpec> groups) {

    public FixedLengthLayoutSpec(Charset charset, int totalLength, List<FixedLengthFieldSpec> fields) {
        this(charset, totalLength, fields, List.of());
    }

    public FixedLengthLayoutSpec {
        charset = charset == null ? StandardCharsets.UTF_8 : charset;
        if (totalLength < 1) {
            throw new IllegalArgumentException("고정길이 전문 전체 길이는 1 이상이어야 합니다.");
        }
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("고정길이 전문 필드 정의가 필요합니다.");
        }
        fields = fields.stream()
                .sorted(Comparator.comparingInt(FixedLengthFieldSpec::start))
                .toList();
        groups = groups == null ? List.of() : List.copyOf(groups);
        validateFields(totalLength, fields);
        validateGroups(totalLength, fields, groups);
    }

    public static FixedLengthLayoutSpec utf8(int totalLength, List<FixedLengthFieldSpec> fields) {
        return new FixedLengthLayoutSpec(StandardCharsets.UTF_8, totalLength, fields);
    }

    public static FixedLengthLayoutSpec utf8(int totalLength, List<FixedLengthFieldSpec> fields, List<FixedLengthGroupSpec> groups) {
        return new FixedLengthLayoutSpec(StandardCharsets.UTF_8, totalLength, fields, groups);
    }

    private static void validateFields(int totalLength, List<FixedLengthFieldSpec> fields) {
        int previousEnd = 0;
        for (FixedLengthFieldSpec field : fields) {
            if (field.zeroBasedStart() < previousEnd) {
                throw new IllegalArgumentException("고정길이 필드가 겹칩니다. name=" + field.name());
            }
            if (field.zeroBasedEndExclusive() > totalLength) {
                throw new IllegalArgumentException("고정길이 필드가 전체 길이를 초과합니다. name=" + field.name());
            }
            previousEnd = field.zeroBasedEndExclusive();
        }
    }

    private static void validateGroups(int totalLength, List<FixedLengthFieldSpec> fields, List<FixedLengthGroupSpec> groups) {
        int fallbackStart = fields.stream()
                .mapToInt(FixedLengthFieldSpec::zeroBasedEndExclusive)
                .max()
                .orElse(0);
        for (FixedLengthGroupSpec group : groups) {
            int groupStart = group.zeroBasedStart(fallbackStart);
            int groupLength = group.itemLength() * group.maxCount();
            if (groupStart + groupLength > totalLength) {
                throw new IllegalArgumentException("반복부가 전체 길이를 초과합니다. name=" + group.name());
            }
            fallbackStart = groupStart + groupLength;
        }
    }
}
