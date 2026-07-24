package com.cpf.core.api.fixedlength;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * count 필드로 건수를 결정하는 반복부 정의입니다.
 *
 * @param name 반복부 이름
 * @param countFieldName 상위 layout의 반복 건수 필드명
 * @param start 1부터 시작하는 byte 위치. 0이면 앞 구간 직후입니다.
 * @param maxCount 최대 반복 건수
 * @param fields 반복 항목 내부의 상대 byte 위치 필드
 */
public record CpfFixedLengthGroupSpec(
        String name,
        String countFieldName,
        int start,
        int maxCount,
        List<CpfFixedLengthFieldSpec> fields) {

    public CpfFixedLengthGroupSpec(
            String name,
            String countFieldName,
            int maxCount,
            List<CpfFixedLengthFieldSpec> fields) {
        this(name, countFieldName, 0, maxCount, fields);
    }

    public CpfFixedLengthGroupSpec {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("고정길이 반복부 이름은 필수입니다.");
        }
        name = name.trim();
        if (countFieldName == null || countFieldName.isBlank()) {
            throw new IllegalArgumentException("고정길이 반복부 count 필드명은 필수입니다. name=" + name);
        }
        countFieldName = countFieldName.trim();
        if (start < 0) {
            throw new IllegalArgumentException("고정길이 반복부 시작 위치는 0 이상이어야 합니다. name=" + name);
        }
        if (maxCount < 1) {
            throw new IllegalArgumentException("고정길이 반복부 최대 건수는 1 이상이어야 합니다. name=" + name);
        }
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("고정길이 반복부 필드 정의가 필요합니다. name=" + name);
        }
        fields = fields.stream()
                .sorted(Comparator.comparingInt(CpfFixedLengthFieldSpec::start))
                .toList();
        validateFields(name, fields);
    }

    public int zeroBasedStart(int fallbackStart) {
        return start > 0 ? start - 1 : fallbackStart;
    }

    public int itemLength() {
        return fields.stream()
                .mapToInt(CpfFixedLengthFieldSpec::zeroBasedEndExclusive)
                .max()
                .orElse(0);
    }

    private static void validateFields(String groupName, List<CpfFixedLengthFieldSpec> fields) {
        int previousEnd = 0;
        Set<String> names = new HashSet<>();
        for (CpfFixedLengthFieldSpec field : fields) {
            if (!names.add(field.name())) {
                throw new IllegalArgumentException(
                        "고정길이 반복부 필드명이 중복되었습니다. group=" + groupName + ", field=" + field.name());
            }
            if (field.zeroBasedStart() < previousEnd) {
                throw new IllegalArgumentException(
                        "고정길이 반복부 필드가 겹칩니다. group=" + groupName + ", field=" + field.name());
            }
            previousEnd = field.zeroBasedEndExclusive();
        }
    }
}
